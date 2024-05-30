#!/bin/bash
/etc/confluent/docker/run &

kafka-topics --create --bootstrap-server localhost:9092 --topic MarketValueTopic --partitions 3
kafka-topics --create --bootstrap-server localhost:9092 --topic PriceTopic --partitions 3
kafka-topics --create --bootstrap-server localhost:9092 --topic ClientHoldingsTopic --partitions 3
kafka-topics --create --bootstrap-server localhost:9092 --topic ObservabilityTopic --partitions 1


wait