package lowerthirds

import java.io._

import io._
import scala.util.matching.Regex


object CommandIndex extends App
{
	val out = new PrintWriter( "src/lowerthirds/resources/indexout" )
	val commands = Source.fromFile( "index" ).getLines.toList.zipWithIndex.sorted
	val line = """([^/]+)(?:/([\w -]+(?:,[\w -]+)*))?/(.*?(?:\.\ \ |$))(.*)"""r
	val numb = """(\d+)"""r
	val brace = """.*[a-z]\{"""r
	val simpleline = "[a-zA-Z]+"r
	
	for ((l, index) <- commands)
	{
		try
		{
			if (simpleline.pattern.matcher( l ).matches)
			{
				out.println( """\hang\m\tb{"\"""" + l + '}' + " -- `\\" + l + "'.")
			}
			else
			{
			val line( c, p, sd, rd ) = l
			val parms =
				if (p eq null) 
					IndexedSeq.empty
				else
					p.split( "," ).toIndexedSeq
			val desc = 
				numb replaceSomeIn (sd,
					m =>
					{
					val before = m.before.charAt( m.before.length - 1 )
					val after = m.after.charAt( 0 )
					
						if ((before.isLetter || before == '"') || (after.isLetter || after == '"'))
							None
						else
							Some( Regex.quoteReplacement("\\s{" + parms(m.group(1).toInt - 1) + "}") )
					}
				)
			val (com, ecom) =
				if (brace.pattern.matcher( c ).matches)
					(c.substring( 0, c.length - 1 ) + " \\{", """ \tb"}"}""")
				else
					(c, "}")
			
				out.println( """\hang\m\hb{\tb{"\"""" + com + '}' +
					(if (parms.isEmpty) "" else parms.mkString( " <", "> <", ">")) + ecom + " -- " + desc)
			}
			
			out.println
		}
		catch
		{
		case e: MatchError =>
			println( "match error on line " + (index + 1) + ": " + l )
			sys.exit
		case e: Exception =>
			println( "error on line " + (index + 1) + ": " + l )
			sys.exit
		}
	}
	
	out.close
}