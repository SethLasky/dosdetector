name := "dosdetector"

version := "0.1"

scalaVersion := "2.12.8"

val fs2Version = "2.1.0"
val circeVersion = "0.11.1"

libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "18.0",
  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

scalacOptions += "-Ypartial-unification"
unmanagedClasspath in Runtime += baseDirectory.value / "etc"


