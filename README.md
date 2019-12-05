# Dos Detector
A small application that reads in a log file as a continuous stream, publishes each line to Kafka, consumes from Kafka, determines whether or not there is a ddos attack from a specific ip address and writes that address to a directory.


## To use
1. Make sure that the values in `etc/application.conf` are correct. There must be a valid directory at the directory path specified.
2. Run the command  `sbt run`

### Dependencies
- Kafka