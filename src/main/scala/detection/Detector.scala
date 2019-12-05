package detection

import java.nio.file.Paths

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, IO}
import cats.effect.concurrent.Ref
import file.DosReport
import fs2.{Stream, io, text}

trait Detector {
  private def detect(dosReports: List[String], perSecondLimit: Int)={
    dosReports.groupBy(ip => ip)
      .values
      .filter(_.size > perSecondLimit)
      .map(_.head)
      .toList
  }

  def readReport(dosReport: DosReport, ref: Ref[IO, Reference], perSecondLimit: Int) ={
    for {
      reference <- ref.get
      result <- if(dosReport.time > reference.time) ref.set(Reference(dosReport.time, List(dosReport.ip))).map{ _ =>
        detect(reference.list, perSecondLimit)
      } else ref.set(Reference(reference.time, reference.list :+ dosReport.ip)).map(_ => List.empty[String])
    } yield result
  }

  def recordDossers(dossers: List[String], directory: String)(blocker: Blocker)(implicit cs: ContextShift[IO], ce: ConcurrentEffect[IO]) ={
    Stream.emits(dossers).flatMap{ s=>
      Stream.emit(s).through(text.utf8Encode).through(io.file.writeAll(Paths.get(directory, s), blocker))
    }
  }
}

case class Reference(time: Long, list: List[String])
