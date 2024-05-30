// const { MongoClient } = require("mongodb");
import { MongoClient } from "mongodb";
import { Kafka } from "kafkajs";
import { handleClientHoldingUpdate, handlePriceUpdate, populatePriceMap, populateHoldingsMap } from "./utils.js";
import EventManager from "./observability/EventManager.js";

// Replace the uri string with your connection string.
const uri = process.env.MONGO_CONNECTION_URI;
console.log(uri, process.env.KAFKA_URL);
const client = new MongoClient(uri);

const kafka = new Kafka({
    clientId: "market-value-calculator",
    brokers: [process.env.KAFKA_URL],
});

const consumer = kafka.consumer({ groupId: "market-value-group" });
console.log("Created consumer");
await consumer.connect();
console.log("Connected");
await consumer.subscribe({ topics: [process.env.PRICE_TOPIC, process.env.CLIENT_HOLDING_TOPIC] /*, fromBeginning: true */ });
console.log("Subscriibed");
const producer = kafka.producer();
console.log("Created producer");
await producer.connect();
console.log("Connected to producer");

// Populate the priceMap with the initial prices
const priceMap = {};
await populatePriceMap(client, priceMap);

// Create event manager
const poolId = "mvc-pool";
let machineId;
try {
    if(process.env.MACHINE_ID != undefined) {
        machineId = Number.parseInt(process.env.MACHINE_ID)
    } else {
        machineId = Math.floor(Math.random() * 100000);
    }
} catch (error) {
    machineId = Math.floor(Math.random() * 100000);
}
const eventManager = new EventManager(producer, machineId);

/**
 * holdingsMap will keep client holdings in memory so that we dont need to query the db everytime
 * {
 *  ticker: {clientID: holding}
 * }
 */
const holdingsMap = {};
await populateHoldingsMap(client, holdingsMap);

let priceUpdatesPerTenSeconds = 0
let clientHoldingUpdatesPerTenSeconds = 0
let marketValueUpdatesPerTenSeconds = 0;

// Send these metrics to the observability framework every Tenseconds
setInterval(() => {
    console.log("Sending metrics...")
    eventManager.sendEvent(poolId, Date.now(), "Price Updates", priceUpdatesPerTenSeconds);
    priceUpdatesPerTenSeconds = 0
    eventManager.sendEvent(poolId, Date.now(), "Client Holding Updates", clientHoldingUpdatesPerTenSeconds);
    clientHoldingUpdatesPerTenSeconds = 0
    eventManager.sendEvent(poolId, Date.now(), "Market Value Updates", marketValueUpdatesPerTenSeconds);
    marketValueUpdatesPerTenSeconds = 0
    eventManager.send1SecondCPUUsage(poolId)
    eventManager.sendMemoryUsage(poolId)
    console.log("Metrics sent!")
}, 10000)

await consumer.run({
    eachMessage: async ({ topic, partition, message, heartbeat, pause }) => {
        console.log({ topic: topic, key: message.key ? message.key.toString() : "NO KEY", value: message.value ? message.value.toString() : "NO VALUE", headers: message.headers });
        const eventId = Math.random() * 100000000000;
        // All messages come in through teh CH topic. We will determine whether this is a price update or a 
        // CH update based on whether there is a "clientId" field and "price" field
        const parsedMsg = JSON.parse(message.value)
        const isClientHoldingUpdate = parsedMsg.clientId != undefined;
        const isPriceUpdate = parsedMsg.price != undefined;

        if (isPriceUpdate) {
            priceUpdatesPerTenSeconds ++;
            eventManager.sendEvent(poolId, eventId, "Price update handler start", Date.now());
            const marketValueUpdates = handlePriceUpdate(priceMap, holdingsMap, JSON.parse(message.value), producer);
            eventManager.sendEvent(poolId, eventId, "Price update handler end", Date.now());
            marketValueUpdatesPerTenSeconds += marketValueUpdates
        } else if(isClientHoldingUpdate) {
            clientHoldingUpdatesPerTenSeconds ++;
            eventManager.sendEvent(poolId, eventId, "Client holdings update handler start", Date.now());
            handleClientHoldingUpdate(priceMap, holdingsMap, JSON.parse(message.value), producer);
            eventManager.sendEvent(poolId, eventId, "Client holdings update handler end", Date.now());
            marketValueUpdatesPerTenSeconds ++;
        } else {
            console.error("Unrecognized message: ", message)
        }
    },
});
