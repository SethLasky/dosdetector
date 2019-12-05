package util

import cats.effect.{Blocker, ContextShift, IO, Timer}
import file.FileReader
import fs2.Stream

import scala.concurrent.ExecutionContext.global
import scala.util.Random

trait TestUtils extends FileReader {
  lazy implicit val ctxShift: ContextShift[IO] = IO.contextShift(global)
  lazy implicit val timer: Timer[IO] = IO.timer(global)

  def dosReportStreamFromFile = {
    val timeFormat = new java.text.SimpleDateFormat("dd/MMMMM/yyyy:HH:mm:ss")
    Stream.resource(Blocker[IO]) flatMap readLogFile(getClass.getResource("/apache-log.txt").getPath)(timeFormat)
  }

  def randomString(length: Int) = Random.alphanumeric.take(length).mkString
}
