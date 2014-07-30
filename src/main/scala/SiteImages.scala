package lowerthirds

import java.io._

import io._
import scala.util.matching.Regex


object SiteImages extends App
{
	val basedir = new File( "/home/ed/Dropbox/projects/lowerthirds-editor/src/docs" )
	val imagesdir = new File( basedir, "images" )
	val imageref = """[[^\]]*]"""
	
	def process( dir: File )
	{
	val subdirs = dir.listFiles( 
		new FileFilter
		{
			def accept( f: File ) = f.isDirectory
		} ).toList
	
	val sources = dir.listFiles(
		new FilenameFilter
		{
			def accept( dir: File, name: String ) = name endsWith ".md"
		} ).toList
		
		if (sources != Nil)
		{
			println( "processing sources in " + dir + "..." )
			
			for (s <- sources)
			{
			val src = Source.fromFile( s ).getLines
			
				
			}
		}
		
		subdirs foreach process
	}
	
	process( basedir )
}