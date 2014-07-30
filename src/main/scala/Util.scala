package lowerthirds

import java.io._


object Util
{
	def readTextFile( s: InputStream ) =
	{
	val r = new BufferedReader( new InputStreamReader(s, "UTF-8") )
	val buf = new StringBuilder
	val chars = new Array[Char]( 10000 )
	var read = 0
	
		while ({read = r.read( chars ); read > -1})
		  buf.appendAll( chars, 0, read )
		
		r.close
		buf.toString
	}
}