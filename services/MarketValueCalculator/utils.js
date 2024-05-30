export async function populatePriceMap(mongoClient, priceMap) {
    try {
        const database = mongoClient.db(process.env.MONGODB_NAME);
        const prices = database.collection(process.env.PRICE_TABLE);
        const cursor = await prices.find();
        // console.log(prices);
        // Print a message if no documents were found
        // if ((await prices.countDocuments({})) === 0) {
        //     console.log("No documents found!");
        // } else {
        //     console.log("There are documents")
        // }
        // Print returned documents
        for await (const result of cursor) {
            // console.log("Price row: ", result);
            priceMap[result.ticker] = {
                ticker: result.ticker,
                price: result.price,
                timestamp: result.timestamp,
            };
        }
        cursor.close();
    } catch (error) {
        console.log(error);
    } finally {
        // Ensures that the client will close when you finish/error
        console.log("Read all prices into price map", Object.keys(priceMap));
    }
}

export async function populateHoldingsMap(mongoClient, holdingsMap) {
    try {
        const database = mongoClient.db(process.env.MONGODB_NAME);
        const holdings = database.collection(process.env.CLIENT_HOLDINGS_TABLE);
        const cursor = await holdings.find();
        for await (const result of cursor) {
            if (!holdingsMap[result.ticker]) {
                holdingsMap[result.ticker] = {};
            }
            holdingsMap[result.ticker][result.clientId] = {
                clientId: result.clientId,
                ticker: result.ticker,
                quantity: result.quantity,
                last_updated: result.last_updated,
            };
        }
        cursor.close();
    } catch (error) {
        console.log(error);
    } finally {
        // Ensures that the client will close when you finish/error
        console.log("Read all holdings into holdings map", holdingsMap);
    }
}

export async function handlePriceUpdate(priceMap, holdingsMap, message, kafkaproducer) {
    priceMap[message.ticker] = {
        ticker: message.ticker,
        price: message.price,
        timestamp: message.timestamp,
    };
    if (holdingsMap[message.ticker]) {
        console.log(holdingsMap[message.ticker])
        let marketValueUpdates = 0
        Object.values(holdingsMap[message.ticker]).forEach(async (holding) => {
            console.log(holding)
            const data = {
                clientId: holding.clientId,
                ticker: message.ticker,
                quantity: holding.quantity,
                price: message.price,
                market_value: holding.quantity * message.price,
                price_last_updated: message.timestamp,
                holding_last_updated: holding.last_updated,
            };
            console.log("Publish to MVC: ", data);
            await publishMarketValue(kafkaproducer, data);
            marketValueUpdates++;
        });
        return marketValueUpdates
    } 
    return 0
}

export async function handleClientHoldingUpdate(priceMap, holdingsMap, message, kafkaproducer) {
    /**
     * Get the latest value of the stock recalculate the market value of this holding
     */
    if (!holdingsMap[message.ticker]) {
        holdingsMap[message.ticker] = {};
    }
    holdingsMap[message.ticker][message.clientId] = {
        clientId: message.clientId,
        ticker: message.ticker,
        quantity: message.quantity,
        last_updated: message.last_updated,
    };
    const data = {
        clientId: message.clientId,
        ticker: message.ticker,
        quantity: message.quantity,
        price: priceMap[message.ticker].price,
        market_value: message.quantity * priceMap[message.ticker].price,
        price_last_updated: priceMap[message.ticker].timestamp,
        holding_last_updated: message.last_updated,
    };
    console.log("Publish to MVC: ", data);
    await publishMarketValue(kafkaproducer, data);
}

async function publishMarketValue(producer, data) {
    try {
        console.log("Attemting to publish message to kafka", data);
        await producer.send({
            topic: process.env.MARKET_VALUE_TOPIC,
            messages: [{ key: data.clientId + "_" + data.ticker, value: JSON.stringify(data) }],
        });
        console.log("Sent message successfully");
    } catch (error) {
        console.error(`Error publishing message: ${error}`);
    }
}
