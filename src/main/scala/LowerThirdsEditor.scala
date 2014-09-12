package lowerthirds

import java.io._
import java.awt.{GraphicsEnvironment, AlphaComposite, Font => JFont}
import java.awt.Font._
import java.awt.image.BufferedImage
import java.awt.RenderingHints.{KEY_ANTIALIASING, VALUE_ANTIALIAS_ON}
import java.awt.Color._
import javax.imageio.{ImageIO}
import javax.swing.undo.{UndoManager, UndoableEdit}
import javax.swing.event.{UndoableEditEvent, UndoableEditListener}
import javax.swing.KeyStroke._

import io.Source
import swing._
import Dialog._
import event._
import Swing._
import BorderPanel.Position._
import collection.mutable.ListBuffer
import scala.util.matching.Regex

import markup.YAML
import typesetter.{Typesetter, Font, Box, Glue}

import Util._


class TS extends YAML
{
	val resource = getClass
	val fs = "/"//System.getProperties.getProperty( "file.separator" )
//	val home = System.getProperties.getProperty( "user.home" ) + fs
//	val fonts = s"${home}Dropbox${fs}Typography${fs}Fonts${fs}"
	val fonts = s"resources/"
//	val cmttf = s"${fonts}bakoma${fs}ttf${fs}"
//	val cmpfb = s"${fonts}bakoma${fs}pfb${fs}"
//	val cmotf = s"${fonts}bakoma${fs}otf${fs}"
	val amspfb = s"${fonts}amsfonts${fs}pfb${fs}"
	val cmttf = s"${fonts}cm-unicode-0.7.0${fs}"
	val gentiumttf = s"${fonts}GentiumPlus-1.510${fs}"

	def out( o: Any ) = LowerThirdsEditor.messages.text = LowerThirdsEditor.messages.text + o.toString + '\n'

		Font.load( resource, s"${cmttf}cmunrm.ttf" )
//		loadFont( "Computer Modern Smallcaps", s"${cmttf}cmcsc10.ttf" )
		Font.load( resource, s"${cmttf}cmunrb.ttf" )
//		loadFont( "Computer Modern Slant", s"${cmttf}cmsl12.ttf" )
//		loadFont( "Computer Modern Italic", s"${cmttf}cmti12.ttf" )
////		loadFont( "Computer Modern Typewriter", s"${cmttf}cmuntt.ttf" )
		Font.preload( resource, s"${cmttf}cmunssdc.ttf" )

		Font.load( resource, s"${cmttf}cmunorm.ttf" )
//		loadFont( "Concrete Bold", s"${cmttf}cmunobx.ttf" )
		Font.load( resource, s"${cmttf}cmunoti.ttf" )

////		loadFont( "Gentium Regular", s"${fonts}GentiumPlus-1.510${fs}GentiumPlus-R.ttf" )
//		loadFont( "Gentium Italic", s"${fonts}GentiumPlus-1.510${fs}GentiumPlus-I.ttf" )
//		loadFont( "Gentium Compact Regular", s"${fonts}GentiumPlusCompact-1.508${fs}GentiumPlusCompact-R.ttf" )
//		loadFont( "Gentium Compact Italic", s"${fonts}GentiumPlusCompact-1.508${fs}GentiumPlusCompact-I.ttf" )
//		loadFont( "Free Helvetian Roman Bold Condensed", s"${fonts}free-helvetian-roman-bold-condensed.pfb" )

//	val CM_PLAIN = Font( "Computer Modern Regular", PLAIN, 30 )
//	val CM_BOLD = Font( "Computer Modern Bold", PLAIN, 30 )
//	val CM_ITALIC = Font( "Computer Modern Italic", PLAIN, 30 )
//	val CM_SLANT = Font( "Computer Modern Slant", PLAIN, 30 )
//	val CM_TYPEWRITER = Font( "Computer Modern Typewriter", PLAIN, 30 )
//	val CM_SMALLCAPS = Font.smallcaps( "Computer Modern Regular", PLAIN, 30 )
//	val CM_SANS_CONDENSED_BOLD = Font( "Computer Modern Sans Condensed Bold", PLAIN, 18 )
//
//	val CONCRETE_PLAIN = Font( "Concrete Regular", PLAIN, 30 )
//	val CONCRETE_BOLD = Font( "Concrete Regular", BOLD, 30 )
//	val CONCRETE_ITALIC = Font( "Concrete Italic", PLAIN, 30 )
//	val CONCRETE_SLANT = Font( "Concrete Regular", ITALIC, 30 )
//	val CONCRETE_SMALLCAPS = Font.smallcaps( "Concrete Regular", PLAIN, 30 )

//	val GENTIUM_PLAIN = Font( "Gentium Regular", PLAIN, 30 )
//	val GENTIUM_BOLD = Font( "Gentium Regular", BOLD, 30 )
//	val GENTIUM_SMALLCAPS = Font.smallcaps( "Gentium Regular", PLAIN, 30 )
//	val GENTIUM_ITALIC = Font( "Gentium Regular", ITALIC, 30 )
//	val GREEK_FONT = Font( "Gentium Regular", PLAIN, 30 )
//	val GENTIUM_SLANT = Font( "Gentium Regular", ITALIC, 30 )
//	val VERSE_FONT = Font( "Free Helvetian Roman Bold Condensed", PLAIN, 18 )
//	val MONO_FONT = Font( "Droid Sans Mono", PLAIN, 30 )

//	def rmfont = CONCRETE_PLAIN
//
//	def bfont = CONCRETE_BOLD
//
//	def scfont = CONCRETE_SMALLCAPS
//
//	def ifont = CONCRETE_ITALIC
//
//	def sfont = CONCRETE_SLANT
//
//	def ttfont = CM_TYPEWRITER
//
//	def vfont = VERSE_FONT
}

object LowerThirdsEditor extends SimpleSwingApplication
{
	val VERSION = "0.4"
	val DATE = "Sep 12, 2014"
	val SHOW_TUTORIAL = false
	val IMAGE_WIDTH = 1280
	val IMAGE_HEIGHT = 720
	val TRANSPARENT = AlphaComposite.getInstance( AlphaComposite.SRC, 0 )
	val fs = "/"//System.getProperties.getProperty( "file.separator" )
	val fonts = s"resources${fs}"
	val FONT =
		JFont.createFont( TRUETYPE_FONT, getClass.getResourceAsStream(s"${fonts}Droid${fs}DroidSansMono.ttf") ).
			deriveFont( PLAIN, 16 )

	lazy val textChooser = new FileChooser
	lazy val messages =
		new TextArea
		{
			editable = false
			font = FONT
		}
	var boxes: List[(String, Box)] = Nil
	lazy val editor = new EditorTextArea
	lazy val overlayFrame =
		new Frame
		{
			peer.setDefaultCloseOperation( javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE )
			title = "Overlays"
			resizable = false
			contents =
				new TabbedPane
				{
					focusable = false

					pages += new TabbedPane.Page( "", new CheckeredTypesetterPanel(null, IMAGE_WIDTH, IMAGE_HEIGHT) )
				}
			pack
		}
	var changesMade = false
	lazy val top: Frame =
		new Frame
		{
			peer.setDefaultCloseOperation( javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE )

			def makeTitle( modified: Boolean ) =
			{
				changesMade = modified
				title = (if (modified) "*" else "") + documentTitle + " - Lower Thirds Editor v" + VERSION + " (" + DATE + ")"
			}

			override def closeOperation = quit

			makeTitle( false )
			location = new Point( 200, 90 )
			editor.status = makeTitle

		var info = true

			reactions +=
			{
				case e: WindowActivated =>
					if (info)
					{
						if (SHOW_TUTORIAL)
							onEDT {tutorialPage.visible = true}

						info = false
					}
			}

			menuBar =
				new MenuBar
				{
					contents +=
						new Menu( "File" )
						{
							contents +=
								new MenuItem(
									Action( "Open" )
									{
										textChooser.showOpenDialog( null ) match
										{
										case FileChooser.Result.Approve =>
											editor.text = readTextFile( new FileInputStream(textChooser.selectedFile) )
											editor.unmodified
											editor.caret.position = 0
										case FileChooser.Result.Cancel =>
										}
									} )
							contents +=
								new MenuItem(
									new Action( "Save" )
									{
										accelerator = Some( getKeyStroke("ctrl S") )

										def apply = save
									} )
							contents +=
								new MenuItem(
									Action( "Save As..." )
									{
										saveAs
									} )
							contents +=
								new MenuItem(
									Action( "Export" )
									{
									val file = textChooser.selectedFile

										if (file eq null)
											messages.text = "can't export: no file has been chosen; do a save"
										else
											export( file, boxes, IMAGE_WIDTH, IMAGE_HEIGHT )
									} )
							contents +=
								new MenuItem(
									Action( "Quit" )
									{
										quit
									} )
						}
					contents +=
						new Menu( "Edit" )
						{
							contents +=
								new MenuItem(
									new Action( "Undo" )
									{
										accelerator = Some( getKeyStroke("ctrl Z") )

										def apply = editor.undo
									} )
							contents +=
								new MenuItem(
									new Action( "Redo" )
									{
										accelerator = Some( getKeyStroke("ctrl Y") )

										def apply = editor.redo
									} )
							contents += new Separator
							contents +=
								new MenuItem(
									new Action( "Copy" )
									{
										accelerator = Some( getKeyStroke("ctrl C") )

										def apply = editor.copy
									} )
							contents +=
								new MenuItem(
									new Action( "Cut" )
									{
										accelerator = Some( getKeyStroke("ctrl X") )

										def apply = editor.cut
									} )
							contents +=
								new MenuItem(
									new Action( "Paste" )
									{
										accelerator = Some( getKeyStroke("ctrl V") )

										def apply = editor.paste
									} )
						}
					contents +=
						new Menu( "Help" )
						{
							contents +=
								new MenuItem(
									Action( "About Lower Thirds Editor" )
									{
									val el = top.location
									val es = top.size
									val as = aboutDialog.size
									val cx = el.getX + es.getWidth/2
									val cy = el.getY + es.getHeight/2

										aboutDialog.location = new Point( (cx - as.getWidth/2).toInt, (cy - as.getHeight/2).toInt )
										aboutDialog.visible = true
									} )
//							contents +=
//								new MenuItem(
//									Action( "Manual" )
//									{
//										manualPage.visible = true
//									} )
//							contents +=
//								new MenuItem(
//									Action( "Markup Language" )
//									{
//										markupPage.visible = true
//									} )
//							contents +=
//								new MenuItem(
//									Action( "Command Reference (by Category)" )
//									{
//										commandCategoryPage.visible = true
//									} )
// 							contents +=
// 								new MenuItem(
// 									Action( "Tutorial" )
// 									{
// 										tutorialPage.visible = true
// 									} )
							contents +=
								new MenuItem(
									Action( "Command Reference (Alphabetical)" )
									{
										commandAlphabeticalPage.visible = true
									} )
						}
				}

			contents =
				new SplitPane( Orientation.Horizontal,
					new ScrollPane( editor )
					{
						preferredSize = (700, 300)
					},
					new ScrollPane( messages )
					{
						preferredSize = (700, 80)
					} )
		}
	lazy val aboutDialog =
		new Dialog( top )
		{
			val ABOUT_WIDTH = 800
			val MARGIN = 10
			val t = new TS

				t('hsize) = ABOUT_WIDTH - 2*MARGIN
				t('vsize) = -1

			val map =
				Map (
					"fs" -> "/",//System.getProperties.getProperty( "file.separator" ),
					"fonts" -> "resources${fs}",
					"cmttf" -> "${fonts}cm-unicode-0.7.0${fs}",
					"VERSION" -> VERSION,
					"DATE" -> DATE
					)
			val b = t processDocument stringReplace(readTextFile(getClass.getResourceAsStream(s"resources${fs}about")), map )

			contents = new TypesetterPanel( b, MARGIN )
			title = "About Lower Thirds Editor"
			modal = true
			resizable = false
			pack
		}
	lazy val tutorialPage = helpPage( "tutorial", "Tutorial", 10, 800, 500 )
	lazy val manualPage = helpPage( "manual", "Manual", 10, 800, 500 )
	lazy val markupPage = helpPage( "language", "Markup Language", 10, 800, 500 )
	lazy val commandCategoryPage = helpPage( "commands", "Categorical Command Reference", 10, 800, 500 )
	lazy val commandAlphabeticalPage = helpPage( "index", "Alphabetical Command Reference", 10, 800, 500 )

	onEDT {overlayFrame.visible = true}

//	onEDT {editor.requestFocusInWindow}

	override def quit
	{
		if (changesMade)
			showConfirmation( message = "You have unsaved material. Save?", messageType = Message.Warning ) match
			{
				case Result.Yes =>
					if (!save)
						return
				case Result.Closed => return
				case _ =>
			}

		sys.exit( 0 )
	}

	def saveAs =
		textChooser.showSaveDialog( null ) match
		{
		case FileChooser.Result.Approve =>
			write
			true
		case FileChooser.Result.Cancel => false
		}

	def titled = textChooser.selectedFile ne null

	def documentTitle =
		if (titled)
			textChooser.selectedFile.getName + " (" + textChooser.selectedFile.getParent + ")"
		else
			"Untitled"

	def save =
		if (titled)
		{
			if (editor.modified)
				write

			true
		}
		else
			saveAs

	def write
	{
	val w = new BufferedWriter( new OutputStreamWriter(new FileOutputStream(textChooser.selectedFile), "UTF-8") )

		w.write( editor.text, 0, editor.text.length )
		w.close
		editor.unmodified
		messages.text = "wrote " + (if (editor.text.isEmpty) 0 else editor.text.count( _ == '\n' ) + 1) + " lines to " + textChooser.selectedFile
	}

	def stringReplace( s: String, replacement: Map[String, String] ): String =
		"""\$\{([a-zA-Z]+)\}""".r replaceAllIn (s, m => Regex.quoteReplacement(stringReplace(replacement(m.group(1)), replacement)))

	def helpPage( page: String, name: String, margin: Int, width: Int, height: Int/*, modalWindow: Boolean = true*/ ) =
		new Dialog( top )
		{
			val fsize = 24
			val t =
				new TS
				{
				val rm = Font( resource, "cmunrm", "plain", fsize )
//				val rm = Font( resource, "Serif", "plain", fsize )
//				val rm = Font( resource, s"${gentiumttf}GentiumPlus-R.ttf", "plain", fsize )

					variable( 'font, rm )
					variable( 'rmfont, rm )
					variable( 'bfont, Font(resource, s"${cmttf}cmunrb.ttf", "plain", fsize) )
					variable( 'scfont, Font(resource, s"${amspfb}cmcsc10.pfb", "plain", fsize) )
					variable( 'ifont, Font(resource, s"${cmttf}cmunti.ttf", "plain", fsize) )
					variable( 'sfont, Font(resource, s"${cmttf}cmunsl.ttf", "plain", fsize) )
					variable( 'tfont, Font(resource, s"${cmttf}cmuntt.ttf", "plain", fsize) )
					variable( 'tbfont, Font(resource, s"${cmttf}cmuntb.ttf", "plain", fsize) )

//					variable( 'bfont, Font(resource, s"Serif", "bold", fsize) )
//					variable( 'scfont, Font(resource, s"${amspfb}cmcsc10.pfb", "plain", fsize) )
//					variable( 'ifont, Font(resource, s"Serif", "slant", fsize) )
//					variable( 'sfont, Font(resource, s"Serif", "slant", fsize) )
//					variable( 'tfont, Font(resource, s"Monospaced", "plain", (fsize*.9).toInt) )
//					variable( 'tbfont, Font(resource, s"Monospaced", "bold", (fsize*.9).toInt) )

//					variable( 'bfont, Font(resource, s"Serif", "bold", fsize) )
//					variable( 'scfont, Font(resource, s"${amspfb}cmcsc10.pfb", "plain", fsize) )
//					variable( 'ifont, Font(resource, s"Serif", "slant", fsize) )
//					variable( 'sfont, Font(resource, s"Serif", "slant", fsize) )
//					variable( 'tfont, Font(resource, s"Monospaced", "plain", (fsize*.9).toInt) )
//					variable( 'tbfont, Font(resource, s"Monospaced", "bold", (fsize*.9).toInt) )
				}

				t('hsize) = width - 2*margin
				t('vsize) = -1

			val b = t processDocumentFromFile( getClass, "resources" + fs + page )

			contents = new ScrollableTypesetterPanel( b, margin, height )
			title = name
			modal = false//modalWindow
			resizable = false
			pack
			centerOnScreen
		}

	object EditorUndoManager extends UndoManager
	{
		def nextUndo = editToBeUndone
	}

	class EditorTextArea extends TextArea
	{
	var status: Boolean => Unit = _		// true means modified

		lineWrap = true
		wordWrap = true
		font = FONT

	private var unmodifiedEdit: UndoableEdit = null
	private val um = EditorUndoManager

		def redo =
			if (um.canRedo)
			{
				um.redo
				status( modified )
			}

		def undo =
			if (um.canUndo)
			{
				um.undo
				status( modified )
			}

		def unmodified
		{
			unmodifiedEdit = um.nextUndo
			status( false )
		}

		def modified = unmodifiedEdit ne um.nextUndo

		peer.getDocument.addUndoableEditListener( um )
		peer.getDocument.addUndoableEditListener(
			new UndoableEditListener
			{
				def undoableEditHappened( e: UndoableEditEvent )
				{
					status( true )
				}
			} )

		reactions +=
		{
			case _ =>
				try
				{
					boxes = typeset( Source.fromString(text).getLines, IMAGE_WIDTH, IMAGE_HEIGHT )
					messages.text = ""

				val tabs = overlayFrame.contents.head.asInstanceOf[TabbedPane]
				val pages = tabs.pages

					for ((name, box) <- boxes)
					{
						pages.find (_.title == name) match
						{
							case None =>
								pages += new TabbedPane.Page( name, new CheckeredTypesetterPanel(box, IMAGE_WIDTH, IMAGE_HEIGHT) )
								tabs.selection.index = pages.last.index
							case Some( page ) =>
								page.content.asInstanceOf[TypesetterPanel].box = box
								overlayFrame.repaint
						}
					}

				var index = 0

					while (index < pages.length)
						if (boxes.exists( _._1 == pages(index).title ))
							index += 1
						else
							pages.remove( index )

					if (pages.isEmpty)
						pages += new TabbedPane.Page( "", new CheckeredTypesetterPanel(null, IMAGE_WIDTH, IMAGE_HEIGHT) )
				}
				catch
				{
					case e: Exception => messages.text = if (e.getMessage eq null) e.toString else e.getMessage
				}
		}
	}

	class ScrollableTypesetterPanel( box: Box, margin: Int, h: Int ) extends
		ScrollPane( new TypesetterPanel(box, margin) )
	{
		preferredSize = (box.width.toInt + 2*margin + new ScrollBar().minimumSize.width + 1, h)
	}

	class TypesetterPanel( var box: Box, margin: Int, w: Int = 0, h: Int = 0 ) extends Panel
	{
	protected val dim =
		if (w == 0)
			(box.width.toInt + 2*margin, box.height.toInt + 2*margin)
		else
			(w + 2*margin, h + 2*margin)

		preferredSize = dim
		focusable = false

		override def paint( g: Graphics2D )
		{
			super.paint( g )

			g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON )

//		val normalizing = GraphicsEnvironment.getLocalGraphicsEnvironment.getDefaultScreenDevice.getDefaultConfiguration.getNormalizingTransform
//
//			g.transform(normalizing)
			paintBackground( g )

			if (box ne null)
				box.draw( g, margin, margin )
		}

		def paintBackground( g: Graphics2D ) {}
	}

	class CheckeredTypesetterPanel( box: Box, w: Int, h: Int ) extends TypesetterPanel( box, 0, w, h )
	{
	val DARK_SQUARE_COLOR = new Color( 0x808080 )
	val LIGHT_SQUARE_COLOR = new Color( 0xB0B0B0 )
	val SQUARE_SIZE = 16

		override def paintBackground( g: Graphics2D )
		{
			g setColor LIGHT_SQUARE_COLOR
			g.fillRect( 0, 0, dim._1, dim._2 )
			g setColor DARK_SQUARE_COLOR

			for (x <- 0 until dim._1 by SQUARE_SIZE)
				for (y <- 0 until dim._2 by SQUARE_SIZE)
					if ((x/SQUARE_SIZE + y/SQUARE_SIZE)%2 == 1)
						g.fillRect( x, y, SQUARE_SIZE, SQUARE_SIZE )
		}
	}

	def export( input: File, images: List[(String, Box)], imageWidth: Int, imageHeight: Int )
	{
	val parent = input.getParentFile
	val pngfilter =
		new FilenameFilter
		{
			def accept( dir: File, name: String) = name endsWith ".png"
		}

		parent.listFiles( pngfilter ) foreach (_.delete)

		for ((file, box) <- images)
		{
		val img = new BufferedImage( imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB_PRE )
		val g = img.getGraphics.asInstanceOf[Graphics2D]

			g setColor BLACK
			g setComposite TRANSPARENT
			g.fillRect( 0, 0, imageWidth, imageHeight )

			g.setRenderingHint( KEY_ANTIALIASING, VALUE_ANTIALIAS_ON )
			box.draw( g, 0, 0 )
			ImageIO.write( img, "PNG", new File(parent, file + ".png") )
		}

		messages.text = "Done exporting"
	}

	def typeset( lines: Iterator[String], boxWidth: Int, boxHeight: Int ) =
	{
		def pathValid( name: String ) =
			name != "" && name != "." && name != ".." && name != " "*name.length &&
			!name.exists( "/\\<>:\"|?*\t\r\n" contains _ )

	val boxes = new ListBuffer[(String, Box)]

		while (lines hasNext)
		{
		val file = lines next

			if (pathValid( file ))
			{
			val buf = new StringBuilder

				def copy: Unit =
					if (lines hasNext)
					{
					val line = lines next

						if (!line.startsWith( "----" ))
						{
							buf append line
							buf append '\n'
							copy
						}
					}

				copy

			val t =
				new TS
				{
				val fsize = 30
				val rm = Font( resource, s"${cmttf}cmunorm.ttf", "plain", fsize )
//				val rm = Font( resource, s"Serif", "plain", fsize )

					variable( 'font, rm )
					variable( 'rmfont, rm )
					variable( 'bfont, Font(resource, s"${cmttf}cmunorm.ttf", "bold", fsize) )
					variable( 'scfont, Font(resource, s"${cmttf}cmunorm.ttf", "smallcaps", fsize) )
					variable( 'ifont, Font(resource, s"${cmttf}cmunoti.ttf", "plain", fsize) )
					variable( 'sfont, Font(resource, s"${cmttf}cmunorm.ttf", "slant", fsize) )
					variable( 'tfont, Font(resource, s"${cmttf}cmuntt.ttf", "plain", fsize) )
					variable( 'tbfont, Font(resource, s"${cmttf}cmuntb.ttf", "plain", fsize) )
//					variable( 'vfont, Font(resource, s"${fonts}free-helvetian-roman-bold-condensed.pfb", "plain", (.6*fsize).toInt) )
					variable( 'vfont, Font(resource, "cmunssdc", "plain", (.6*fsize).toInt) )

//					variable( 'bfont, Font(resource, s"Serif", "bold", fsize) )
//					variable( 'scfont, Font(resource, s"${amspfb}cmcsc10.pfb", "plain", fsize) )
//					variable( 'ifont, Font(resource, s"Serif", "slant", fsize) )
//					variable( 'sfont, Font(resource, s"Serif", "slant", fsize) )
//					variable( 'tfont, Font(resource, s"Monospaced", "plain", fsize) )
//					variable( 'tbfont, Font(resource, s"Monospaced", "bold", fsize) )
//					variable( 'vfont, Font(resource, s"Monospaced", "bold", (.6*fsize).toInt) )
				}

				t('color) = WHITE
				t('vcolor) = WHITE
				t('hsize) = boxWidth

				boxes += file -> t.processDocument( buf.toString )
			}
			else
				sys.error( "invalid filename: \"" + file + '"' )
		}

		boxes.toList
	}
}
