# Market Value Calculator

The Market Value Calculator (MVC) service listens for price updates and updates to client portfolios. When either of those updates are received the MVC will recalculate the affected holdings of the affected clients and publish the new information.

## Schema
```
{
    clientID: string,
    ticker: string,
    quantity: integer,
    price: double,
    market value: double,
    price_last_updated: long,
    holding_last_updated: long
}
```

Example:
John's portfolio contains 10 shares of APPL priced at $150/share. If the price of APPL increased to $160/share, the MVC will publish a new message to the Market Value Kafka Topic that looks like this:
```
{
    clientID: <John's client ID>,
    ticker: "APPL",
    quantity: 10,
    price: 160,
    market value: 1600,
    price_last_updated: <milliseconds since New Year GMT>,
    holding_last_updated: <milliseconds since New Year GMT>,
}
```
The kafka topic will automatically load these updates into a mongo collection called "MarketValue" where the _id is a composite of the clientID and the ticker.


## Notes

This service depends on three kafka topics which must be configured in the environment variables:
- PRICE_TOPIC
- CLIENT_HOLDING_TOPIC
- MARKET_VALUE_TOPIC
The Kafka connection URI needs to be configured:
- KAFKA_URL
It also depends on two mongoDB tables which must be similarly configured:
- CLIENT_HOLDINGS_TABLE
- PRICE_TABLE
The MongoDB connection URI needs to be configured
- MONGO_CONNECTION_URI

You can set the PUBLISH_TO_KAFKA environment variable to false if you just want the output to be written to the console. 