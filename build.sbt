name := "dosdetector"

version := "0.1"

scalaVersion := "2.12.8"

val circeVersion = "0.11.0"

libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "18.0",
  "co.fs2" %% "fs2-io" % "2.1.0",
  "com.ovoenergy" %% "fs2-kafka" % "0.20.2",
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
)

scalacOptions += "-Ypartial-unification"
unmanagedClasspath in Runtime += baseDirectory.value / "etc"


