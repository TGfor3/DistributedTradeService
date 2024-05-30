from kafka import KafkaConsumer, TopicPartition

import threading
import logging
import json
import time

# Copied over from Eli's github for now, we will have to combine eventually though


class PricesManager():
    def __init__(self, logger, use_kafka=True) -> None:

        # To consume latest messages and auto-commit offsets
        self.prices = dict()
        self.use_kafka = use_kafka
        if use_kafka:
            self.consumer = Consumer(self.prices)
            self.consumer.start()
        self.logger = logger
        self.logger.info("[PricesManager] prices manager initialized")

    def get_price(self, ticker):
        self.logger.info(f"[PricesManager] getting price for {ticker}.")
        if ticker in self.prices:
            return self.prices[ticker]["price"]
        else:
            return 100

    def close(self):
        if self.use_kafka:
            self.consumer.stop()


class Consumer(threading.Thread):
    def __init__(self, price_map):
        threading.Thread.__init__(self)
        self.stop_event = threading.Event()
        self.price_map = price_map

    def stop(self):
        self.stop_event.set()

    def run(self):
        consumer = KafkaConsumer('PriceTopic',
                                 group_id='price-group',
                                 bootstrap_servers=['localhost:9092'],
                                 value_deserializer=lambda m: json.loads(m.decode('ascii')))

        # TODO populate the map with the initial stock prices

        while not self.stop_event.is_set():
            for message in consumer:
                # print(message.offset, message.value)
                self.price_map[message.value['ticker']] = message.value
                if self.stop_event.is_set():
                    break

        consumer.close()


if __name__ == "__main__":

    mgr = PricesManager(logging, use_kafka=False)
    time.sleep(5)
    print(f"price for A: {mgr.get_price('A')}")
    print(f"price for AA: {mgr.get_price('AA')}")
    print(f"price for AACG: {mgr.get_price('AACG')}")

    time.sleep(5)
    print(f"price for A: {mgr.get_price('A')}")
    print(f"price for AA: {mgr.get_price('AA')}")
    print(f"price for AACG: {mgr.get_price('AACG')}")

    try:
        while 1:
            time.sleep(.1)
    except KeyboardInterrupt:
        print("attempting to close threads.")
        mgr.close()
        print("threads successfully closed")
