name := "spark-streaming-app"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq (
  "com.typesafe" % "config" % "1.3.0",
  "org.apache.spark" %% "spark-core" % "2.1.0" % "provided",
  "org.apache.spark" %% "spark-streaming" % "2.1.0" % "provided",
  "org.apache.spark" %% "spark-sql" % "2.1.0" % "provided",
  "org.apache.bahir" %% "spark-streaming-twitter" % "2.0.1"
)

mainClass in (Compile, run) := Some("main.Main")

mainClass in assembly := Some("main.Main")

assemblyJarName in assembly := "spark-streaming-app.jar"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _) => MergeStrategy.discard
  case x => MergeStrategy.first
}