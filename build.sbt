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

assemblyMergeStrategy in assembly := {
  case PathList("module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/module-info.class") => MergeStrategy.discard
  case x if x.endsWith("/io.netty.versions.properties") => MergeStrategy.discard
  case x if x.matches("google/protobuf/.*\\.proto") => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}
libraryDependencies ++= Seq("org.slf4j" % "slf4j-api" % "1.7.32",
  "org.slf4j" % "slf4j-simple" % "1.7.32")


libraryDependencies += ("com.crobox.clickhouse" %% "client" % "1.0.0")

libraryDependencies += "org.typelevel" %% "cats-core" % "2.3.0"
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.5.3"

libraryDependencies += "co.fs2" %% "fs2-core" % "2.5.10"

libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
)

enablePlugins(Fs2Grpc)
libraryDependencies += "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion
Compile / PB.protoSources := Seq(baseDirectory.value / "opentelemetry-proto")

mainClass in (Compile, run) := Some("io.logtemplate.Main")