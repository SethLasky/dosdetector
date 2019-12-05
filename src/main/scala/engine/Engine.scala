package engine

import cats.effect.concurrent.Ref
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.config.ConfigFactory
import config.DetectorConfig
import detection.{Detector, Reference}
import file.FileReader
import fs2.kafka._
import kafka.KafkaClient
import io.circe.config.syntax._
import io.circe.generic.auto._
import fs2.Stream


object Engine extends IOApp with FileReader with KafkaClient with Detector{

  def run(args: List[String]): IO[ExitCode] = {
    val stream = for {
      config <- Stream.eval(IO.fromEither(ConfigFactory.load.as[DetectorConfig]))
      initialReference = Reference(0, List())
      ref <- Stream.eval(Ref[IO].of(initialReference))
      blocker <- Stream.resource(Blocker[IO])
      timeFormat = new java.text.SimpleDateFormat(config.timeFormat)
      producerSettings = ProducerSettings[IO, String, String].withBootstrapServers(config.kafka.host)
      consumerSettings = ConsumerSettings[IO, String, String]
        .withEnableAutoCommit(true)
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers(config.kafka.host)
        .withGroupId(config.kafka.groupId)

      producer <- producerStream[IO].using(producerSettings)
      consumer <- consumerStream[IO].using(consumerSettings).evalTap(_.subscribeTo(config.kafka.topic))

      dosReportStream = readLogFile(config.logPath)(timeFormat)(blocker).filter(_.isRight).map(_.right.get)
      publishStream = dosReportStream flatMap produceDosReports(producer, config.kafka.topic)
      consumedDosReport <- consumeDosReports(consumer).filter(_.isRight).map(_.right.get) concurrently publishStream
      dossers <- Stream.eval(readReport(consumedDosReport, ref, config.allowedPerSecond))
      recorded <- recordDossers(dossers, config.directoryPath)(blocker)

    } yield recorded

    stream.compile.drain.as(ExitCode.Success)
  }
}
