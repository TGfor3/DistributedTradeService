from kafka import KafkaProducer
import json
from json import dumps

p = KafkaProducer(bootstrap_servers = ['kafka:29092'], value_serializer = lambda x:dumps(x).encode('utf-8'))

data = {'ticker': 'ABC', 'price': 111.11}

p.send('PriceTopic', value = data)

p.flush()