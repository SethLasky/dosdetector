package file

import java.nio.file.Paths

import fs2.{Pipe, Stream, text}
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, IO}

trait FileReader {
  private def readFile(source: String)(blocker: Blocker)(implicit cs: ContextShift[IO], ce: ConcurrentEffect[IO]) =
    fs2.io.file.readAll[IO](Paths.get(source), blocker, 10000) through text.utf8Decode through text.lines

  private def reportPipe(timeFormat: java.text.SimpleDateFormat)(blocker: Blocker)(implicit cs: ContextShift[IO], ce: ConcurrentEffect[IO]): Pipe[IO, String, Either[Throwable, DosReport]] = _ flatMap lineToDosReport(blocker)(timeFormat)

  private def lineToDosReport(blocker: Blocker)(timeFormat: java.text.SimpleDateFormat)(reportLine: String)(implicit cs: ContextShift[IO], ce: ConcurrentEffect[IO]) = Stream.eval(IO{
    val split = reportLine.split(" ")
    val ip = split.head
    val time = formatDate(split(3).stripPrefix("["))(timeFormat)
    DosReport(ip, time)
  }.attempt)

  private def formatDate(dateString: String)(timeFormat: java.text.SimpleDateFormat) = {
    timeFormat.parse(dateString).getTime
  }

  def readDosReport(source: String)(timeFormat: java.text.SimpleDateFormat)(blocker: Blocker)(implicit cs: ContextShift[IO], ce: ConcurrentEffect[IO]) = readFile(source)(blocker) through reportPipe(timeFormat)(blocker)
}

case class DosReport(ip: String, time: Long)
