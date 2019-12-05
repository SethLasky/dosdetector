package kafka

import cats.effect.IO
import fs2.Stream
import fs2.kafka._
import org.scalatest.{Matchers, WordSpecLike}
import util.TestUtils
import scala.util.Random

class KafkaClientTest extends WordSpecLike with Matchers with KafkaClient with TestUtils {

  "KafkaClient" must {
    "produce messages to a kafka topic and be able to consume from that topic" in {
      val kafkaUrl = "localhost:9092"
      val topic = randomString(10)
      val dosReportStream = dosReportStreamFromFile.filter(_.isRight).map(_.right.get).take(5000)
      val producerSettings = ProducerSettings[IO, String, String].withBootstrapServers(kafkaUrl)
      val consumerSettings = ConsumerSettings[IO, String, String]
          .withEnableAutoCommit(true)
          .withAutoOffsetReset(AutoOffsetReset.Earliest)
          .withBootstrapServers(kafkaUrl)
          .withGroupId(topic)

      val publishStream = for {
        producer <- producerStream[IO].using(producerSettings)
        published <- dosReportStream flatMap produceDosReports(producer, topic)
      } yield published

      val consumedStream = for {
        consumer <- consumerStream[IO].using(consumerSettings).evalTap(_.subscribeTo(topic))
        consumed <- consumeDosReports(consumer).filter(_.isRight).map(_.right.get)
      } yield consumed

      val fullStream = consumedStream.take(5000) concurrently publishStream

      fullStream.compile.toList.unsafeRunSync() shouldBe dosReportStream.compile.toList.unsafeRunSync()
    }
  }
}
