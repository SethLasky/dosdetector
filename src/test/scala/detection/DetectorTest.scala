package detection

import java.io.File

import cats.effect.{Blocker, IO}
import cats.effect.concurrent.Ref
import fs2.Stream
import org.scalatest.{Matchers, WordSpecLike}
import util.TestUtils

class DetectorTest extends WordSpecLike with Matchers with TestUtils with Detector {

  "Detector" must {
    "read in the next dos report and update the state of the program as well as write a file for each dosser detected" in {
      val initialReference = Reference(0, List())
      val perSecondLimit = 3
      val testDirectory = randomString(10)
      val directory = new File(testDirectory)
      directory.mkdir()
      val directoryPath = directory.getPath

      val stream = for{
        blocker <- Stream.resource(Blocker[IO])
        ref <- Stream.eval(Ref[IO].of(initialReference))
        dosReport <- dosReportStreamFromFile.filter(_.isRight).map(_.right.get).take(50000)
        dossers <- Stream.eval(readReport(dosReport, ref, perSecondLimit))
        record <- recordDossers(dossers, directoryPath)(blocker)
      } yield record

      stream.compile.toList.unsafeRunSync()
      directory.listFiles shouldNot have size 0
      directory.listFiles.foreach(_.delete())
      directory.delete()
    }
  }
}
