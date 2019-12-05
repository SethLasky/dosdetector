package file

import cats.effect.{Blocker, ContextShift, IO, Timer}
import fs2.Stream
import org.scalatest.{Matchers, WordSpecLike}
import util.TestUtils

import scala.concurrent.ExecutionContext.global

class FileReaderTest extends WordSpecLike with Matchers with TestUtils {

  "FileReader" must {
    "read a log file into a stream of reports" in {
      val list = dosReportStreamFromFile.compile.toList.unsafeRunSync()
      list should not be empty
      list.exists(_.isRight) shouldBe true
    }
  }
}
