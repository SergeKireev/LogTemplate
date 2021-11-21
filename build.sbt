name := "log-template"

version := "0.1"

scalaVersion := "2.13.7"
scalacOptions += "-Ymacro-annotations"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.9"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"
libraryDependencies += ("com.github.tototoshi" %% "scala-csv" % "1.3.8")

val circeVersion = "0.14.1"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

val elastic4sVersion = "7.15.1"
libraryDependencies ++= Seq(
  // recommended client for beginners
  ("com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % elastic4sVersion),
  // test kit
  "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % "test"
)

libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.32",
  "org.slf4j" % "slf4j-simple" % "1.7.32")


libraryDependencies += ("com.crobox.clickhouse" %% "client" % "1.0.0")

libraryDependencies += "org.typelevel" %% "cats-core" % "2.3.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.5.3"

libraryDependencies += "co.fs2" %% "fs2-core" % "2.5.10"

mainClass in (Compile, run) := Some("io.logtemplate.Main")