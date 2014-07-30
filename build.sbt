name := "LTEditor"

version := "0.1"

scalaVersion := "2.11.2"

scalacOptions ++= Seq( "-deprecation", "-feature", "-language:postfixOps", "-language:implicitConversions", "-language:existentials" )

incOptions := incOptions.value.withNameHashing( true )

organization := "org.lteditor"

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "1.0.1"

libraryDependencies ++= Seq(
	"org.lteditor" %% "typesetter" % "0.1",
	"org.lteditor" %% "markup" % "0.1"
	)

mainClass in (Compile, packageBin) := Some( "lowerthirds.LowerThirdsEditor" )

mainClass in (Compile, run) := Some( "lowerthirds.LowerThirdsEditor" )

//retrieveManaged := true

//offline := true
