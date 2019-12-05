package config

import com.google.common.io.Resources

case class DetectorConfig(directory: String, log: String, timeFormat: String, allowedPerSecond: Int, kafka: Kafka){
  val logPath = Resources.getResource(log).getFile
  println(logPath)
  val directoryPath = Resources.getResource(directory).getPath
}

case class Kafka(host: String, topic: String, groupId: String)