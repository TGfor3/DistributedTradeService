// Switch to CommandDB and create collections and insert documents
db = db.getSiblingDB('CommandDB');

db.createCollection("clientPortfolio", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["quantity"],
         properties: {
            quantity: {
               bsonType: "int",
               minimum: 0,
               description: "must be an integer and cannot be negative"
            }
         }
      }
   }
});

db.clientPortfolio.createIndex(
   { clientID: 1, ticker: 1 },
   { unique: true }
);

db.clientPortfolio.insertOne({ clientID: "exampleClientId", ticker: "exampleticker", quantity: 11, timestamp: new Date(), lastUpdated: new Date() });

db.createCollection("stageClientPortfolio");
db.stageClientPortfolio.insertOne({ clientID: "exampleClientId", ticker: "exampleticker", quantity: 11, timestamp: new Date(), lastUpdated: new Date() });


db.createCollection("transactions");
db.transactions.insertOne({ txnID: "exampleTxnIdYY", clientID: "exampleClientId", ticker: "exampleticker", quantity: 5, buySell: "buy", price: 150.60, timestamp: new Date(), lastUpdated: new Date() });

//db.createCollection("stockInventory")
//db.stockInventory.insertOne({ticker: "$TEMP",quantity: 0,timestamp: new Date(),lastUpdated: new Date()})

// Switch to QueryDB and do the same
db = db.getSiblingDB('QueryDB');

db.createCollection("clientPortfolio", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["quantity"],
         properties: {
            quantity: {
               bsonType: "int",
               minimum: 0,
               description: "must be an integer and cannot be negative"
            }
         }
      }
   }
});

db.clientPortfolio.createIndex(
   { clientID: 1, ticker: 1 },
   { unique: true }
);

db.clientPortfolio.insertOne({ clientID: "exampleClientId", ticker: "exampleticker", quantity: 11, timestamp: new Date(), lastUpdated: new Date() });


db.createCollection("transactions");
db.transactions.insertOne({ txnID: "exampleTxnIdYY", clientID: "exampleClientId", ticker: "exampleticker", quantity: 5, buySell: "buy", price: 150.60, timestamp: new Date(), lastUpdated: new Date() });

db.createCollection("stageClientPortfolio");

db.stageClientPortfolio.createIndex(
   { txnID: 1},
   { unique: true }
);


db.createCollection("stageStockInventory")

db.stageStockInventory.createIndex(
   { txnID : 1},
   { unique: true }
);
