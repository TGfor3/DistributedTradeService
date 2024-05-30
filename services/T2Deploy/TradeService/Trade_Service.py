from PricesManager import PricesManager
from flask import Flask, jsonify
from flask import request
from TransactionIDGenerator import TransactionIDGenerator
from TransactionDetails import TransactionDetails, TransactionType
import pika
import os
from pymongo import MongoClient
from datetime import datetime

# TODO: give buy and sell their own channels if needed
print("Starting Trade Service")

# read rabbitmq connection url from environment variable
try:
    amqp_url = os.environ['AMQP_URL']
    # amqp_host = os.environ['RABBITMQ_HOST']
    # amqp_pass = os.environ['RABBITMQ_USERPASS']
    # amqp_user = os.environ['RABBITMQ_USERPASS']
except Exception:
    print("AMQP_URL not an environment variable")
    amqp_url = "amqp://rabbit_mq?connection_attempts=10&retry_delay=10"
    # amqp_host = 'rabbitmq'
    # amqp_pass = 'guest'
    # amqp_user = 'guest'

try:
    mongo_url = os.environ['MONGO_URL']
except Exception:
    print("MONGO_URL not an environment variable")
    mongo_url = "mongodb://isigutt:isi@localhost:27017/"


# credentials = pika.PlainCredentials(amqp_user, amqp_pass)
# parameters = pika.ConnectionParameters(amqp_host, 5672, '/', credentials)
# url_params = pika.URLParameters(amqp_url)
# connect to rabbitmq
global connection
global channel
connection = pika.BlockingConnection(pika.URLParameters(amqp_url))
channel = connection.channel()
queue_name = os.environ["TRADE_QUEUE_NAME"]
channel.queue_declare(queue=queue_name, durable=True)

print(channel.is_open)
print(connection.is_open)

if not channel.is_open:
    print("Channel is not open. Reconnecting...")
    # Re-establish connection and channel
if not connection.is_open:
    print("Connection is not open. Reconnecting...")
    # Re-establish connection


# MongoDB connections
db_name1 = "CommandDB"  # Modify with your actual DB name
db_name2 = "QueryDB"  # Modify with your actual DB name
collection_name = "clientPortfolio"  # Modify with your actual collection name

# Connect to MongoDB
client = MongoClient(mongo_url)
commandDB = client[db_name1]
queryDB = client[db_name2]
commandCollection = commandDB[collection_name]
queryCollection = queryDB[collection_name]


tx_generator = TransactionIDGenerator()
# Use Kafka is false for now, so we don't have to spin that up, we'll wait for integration
# price_Manager = PricesManager(None, False)

app = Flask(__name__)


@app.route('/buy', methods=['GET', 'POST'])
def buy():
    # Implement your logic
    data = request.get_json()
    tx_ID = tx_generator.get_and_increment()
    submitBuy(data['clientID'], data['ticker'], data['amount'], tx_ID)
    return jsonify(message='Buy endpoint reached', txnID=tx_ID), 200


@app.route('/sell', methods=['GET', 'POST'])
def sell():
    data = request.get_json()
    tx_ID = tx_generator.get_and_increment()
    submitSell(data['clientID'], data['ticker'], data['amount'], tx_ID)
    return jsonify(message='Sell endpoint reached', txnID=tx_ID), 200


@app.route('/register', methods=['GET', 'POST'])
def register():
    data = request.get_json()
    result = submit_register(data['clientID'], data['amount'])
    print(result)
    if result:
        return jsonify(message='New client endpoint reached'), 200
    else:
        return jsonify(message='Error submitting registration'), 500


# method should add the TransactionDetails impl to the rabbit queue
def submitBuy(clientID: int, ticker: str, buyAmount: int, txnID):
    try:
        lockinPrice = price_Manager.get_price(ticker)
    except Exception:
        print("Issue with prices manager")
        lockinPrice = 20
    details = TransactionDetails(clientID, ticker, buyAmount,
                                 TransactionType.BUY, lockinPrice, txnID)

    check_rabbit_connection()
    channel.basic_publish(
        exchange='',
        routing_key=queue_name,
        body=details.serialize(),
        properties=pika.BasicProperties(delivery_mode=2)
    )
    print("Buy request submitted")
    print(details)


def submitSell(clientID: int, ticker: str, sellAmount: int, txnID):
    try:
        lockinPrice = price_Manager.get_price(ticker)
    except Exception:
        print("Issue with prices manager")
        lockinPrice = 20
    details = TransactionDetails(clientID, ticker, sellAmount,
                                 TransactionType.SELL, lockinPrice, txnID)

    check_rabbit_connection()
    channel.basic_publish(
        exchange='',
        routing_key=queue_name,
        body=details.serialize(),
        properties=pika.BasicProperties(delivery_mode=2)
    )

    print("Sell request submitted")
    print(details)

# method should directly add client to the Command and Query DBs


def submit_register(clientID: int, startingBalance: str):
    client_info = {
        "clientID": clientID,
        "ticker": 'CASH',
        "quantity": startingBalance
    }
    try:
        # commandCollection.insert_one(client_info.copy())
        queryCollection.insert_one(client_info.copy())
    except Exception:
        return False
    # TODO: add in fault tolerance
    print(
        f"New client added\n  Client ID: {clientID}\n  Starting Balance: {startingBalance}\n")
    return True


def check_rabbit_connection():
    global channel
    global connection
    # connect to channel then connection
    if (connection.is_closed):
        print("Connection was closed")
        connection = pika.BlockingConnection(amqp_url)
        print("reopened connection")
        channel = connection.channel()

    connection = pika.BlockingConnection(pika.URLParameters(amqp_url))


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
