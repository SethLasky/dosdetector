package file

import cats.effect.{Blocker, ContextShift, IO, Timer}
import fs2.Stream
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext.global

class FileReaderTest extends WordSpecLike with Matchers with FileReader {

  lazy implicit val ctxShift: ContextShift[IO] = IO.contextShift(global)
  lazy implicit val timer: Timer[IO] = IO.timer(global)
  "FileReader" must {
    "read a log file into a stream of reports" in {
      val timeFormat = new java.text.SimpleDateFormat("dd/MMMMM/yyyy:HH:mm:ss")
      val stream = Stream.resource(Blocker[IO]) flatMap readDosReport(getClass.getResource("/apache-log.txt").getPath)(timeFormat)
      val list = stream.compile.toList.unsafeRunSync()
      list should not be empty
      list.exists(_.isRight) shouldBe true
    }
  }
}
