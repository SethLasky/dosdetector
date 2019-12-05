package kafka

import cats.effect.{Concurrent, ContextShift, IO, Timer}
import file.DosReport
import fs2.kafka._
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._


trait KafkaClient {
  def consumeDosReports(consumer: KafkaConsumer[IO, String, String])(implicit ctxShift: ContextShift[IO], timer: Timer[IO], concurrent: Concurrent[IO]) =
    consumer.stream map parseDosReport

  private def parseDosReport(message: CommittableConsumerRecord[IO, String, String]) = decode[DosReport](message.record.value) match {
    case Right(dosReport) => Right(dosReport)
    case Left(err) => Left(new Error(s"There was an error decoding the message: ${message.record.value}. Error: $err"))
  }

  def produceDosReports(dosReport: DosReport, producer: KafkaProducer[IO, String, String], topic: String)
                       (implicit ctxShift: ContextShift[IO], timer: Timer[IO], concurrent: Concurrent[IO]) = {
    val producerRecord = transformToProducerRecord(dosReport, topic)
    val producerMessage = ProducerRecords.one(producerRecord)
    producer.produce(producerMessage)
  }

  private def transformToProducerRecord(dosReport: DosReport, topicName: String) = {
    val reportString = dosReport.asJson.toString()
    ProducerRecord(topicName, reportString, reportString)
  }
}
