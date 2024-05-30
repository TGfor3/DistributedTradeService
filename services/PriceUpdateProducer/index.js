/**
 * To run locally: node --env-file=.env .\index.js
 */

import FastNoiseLite from "fastnoise-lite";
import { Kafka } from "kafkajs";
import Fs from "fs";
import CsvReadableStream from "csv-reader";
import EventManager from './observability/EventManager.js'

const publishToKakfa = process.env.PUBLISH_TO_KAFKA == "TRUE";

const kafka = publishToKakfa
    ? new Kafka({
          clientId: "stock-price-producer",
          brokers: [process.env.KAFKA_URL], // Update with your Kafka brokers
      })
    : "";

const producer = publishToKakfa ? kafka.producer() : "";
let observability;
if (publishToKakfa) {
    console.log("Attempting to connect");
    await producer.connect();
    observability = new EventManager(producer)
}



function readTickerFile() {
    const stocks = [];
    let inputStream = Fs.createReadStream(process.env.TICKER_FILE_PATH, "utf8");
    let firstRow = true;
    return new Promise((resolve, reject) => {
        inputStream
            .pipe(new CsvReadableStream({ parseNumbers: true, parseBooleans: true, trim: true }))
            .on("data", function (row) {
                if (firstRow) {
                    firstRow = false;
                    return;
                }
                stocks.push({
                    ticker: row[0],
                    price: Number.parseFloat(row[2].replaceAll("$", "")),
                    step: 0,
                });
            })
            .on("end", function () {
                console.log("No more rows!");
                resolve(stocks);
            });
    });
}
const stocks = await readTickerFile();
console.log("READ!");
const noise = new FastNoiseLite();
const seed = process.env.SEED;

let expectedReturn = 1.003;
let stdDeviation = 0.003;

function geometricBrownianMotion(currentPrice, time) {
    const drift = currentPrice * expectedReturn;
    const shock = currentPrice * stdDeviation * r(time);
    if (drift + shock < 0.01) return 0.01;
    return drift + shock;
}

function r(time) {
    return noise.GetNoise(time * 50000 + seed, 0);
}

function sleep(time) {
    return new Proise((resolve, reject) => {
        setTimeout(() => {
            resolve();
        }, time);
    });
}

function priceProducer() {
    let time = 0;
    setInterval(async () => {
        const index = Math.floor(Math.random() * stocks.length);
        const stock = stocks[index];

        stock["price"] = geometricBrownianMotion(stock["price"], ++stock["step"]);
        time++;

        sendPriceUpdateToKafka(stock["ticker"], stock["price"])
    }, process.env.INTERVAL);
}

async function sendPriceUpdateToKafka(ticker, price) {
    const message = {
        ticker,
        price,
        timestamp: Date.now(),
    };
    try {
        if (publishToKakfa) {
            await producer.send({
                topic: process.env.KAFKA_PRICE_TOPIC_NAME,
                messages: [{ value: JSON.stringify(message) }],
            });
        }
        console.log(`[T=${message.timestamp}] ${ticker} price: ${price}`);
    } catch (error) {
        console.error(`Error publishing ${ticker} price: ${error} to price topic`);
    }

    try {
        if (publishToKakfa) {
            await producer.send({
                topic: process.env.KAFKA_CLIENT_HOLDINGS_TOPIC_NAME,
                messages: [{ key: ticker, value: JSON.stringify(message) }],
            });
        }
        console.log(`[T=${message.timestamp}] ${ticker} price: ${price}`);
    } catch (error) {
        console.error(`Error publishing ${ticker} price: ${error} to client holdings topic`);
    }

    if(observability) {
        observability.sendEvent("price-producer-pool", Math.floor(Math.random() * 10000) + Date.now() + "", ticker, price)
    }

    
}

priceProducer();
