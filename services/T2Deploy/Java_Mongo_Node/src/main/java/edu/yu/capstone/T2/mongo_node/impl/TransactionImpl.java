package edu.yu.capstone.T2.mongo_node.impl;

import com.mongodb.MongoWriteException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;

import edu.yu.capstone.T2.mongo_node.*;
import edu.yu.capstone.T2.mongo_node.MongoDBSchema.KeyNames;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.bson.BSONObject;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.print.Doc;
import javax.xml.crypto.dsig.keyinfo.KeyName;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionImpl extends RepositoryPatternBase {

        MongoDatabase mongoDb;
        String mongoURI;
        String queryDB = "QueryDB";
        Logger logger = Logger.getLogger(TransactionImpl.class.getName());
        private KafkaProducer<String, String> kafkaProducer;

        public TransactionImpl(String URI, KafkaProducer<String, String> kafkaProducer) {
                super(URI, kafkaProducer);
                this.kafkaProducer = kafkaProducer;
                this.queryDB = "QueryDB";
                this.mongoURI = URI;
                // this.logger.setLevel(Level.WARNING);

                connect();
        }

        /**
         * Initiate this Objects connection to mongo and sets the mongoDB variable
         *
         */
        @Override
        public void connect() {
                MongoClient mongoClient = MongoClients.create(mongoURI);
                mongoDb = mongoClient.getDatabase(queryDB);
                logger.info("Reconnected to MongoDB successfully.");
                // try {
                // // Close the existing client connection if it exists
                // if (mongoClient != null) {
                // mongoClient.close();
                // }
                //
                // // Re-establish the connection using the stored URI
                // mongoClient = MongoConnectionManager.getClient(mongoURI);
                // mongoDb = mongoClient.getDatabase(queryDB);
                // logger.info("Reconnected to MongoDB successfully.");
                //
                // // Reinitialize collections
                // this.stockInventory =
                // mongoDb.getCollection(MongoDBSchema.STOCK_INVENTORY.getTableName());
                // this.stageStockInventory =
                // mongoDb.getCollection(MongoDBSchema.STAGE_STOCK_INVENTORY.getTableName());
                // this.clientPortfolio =
                // mongoDb.getCollection(MongoDBSchema.CLIENT_PORTFOLIO.getTableName());
                // this.stageClientPortfolio =
                // mongoDb.getCollection(MongoDBSchema.STAGE_CLIENT_PORTFOLIO.getTableName());
                // this.transactions =
                // mongoDb.getCollection(MongoDBSchema.TRANSACTIONS.getTableName());
                //
                // logger.info("MongoDB collections reinitialized successfully.");
                //
                // } catch (Exception e) {
                // logger.severe("Failed to reconnect to MongoDB: " + e.getMessage());
                // }
        }

        @Override
        protected boolean stageBrokerBuy(TransactionDetails tx) {
                MongoCollection<Document> stockInventory = mongoDb
                                .getCollection(MongoDBSchema.STOCK_INVENTORY.getTableName());
                MongoCollection<Document> stageStockInventory = mongoDb
                                .getCollection(MongoDBSchema.STAGE_STOCK_INVENTORY.getTableName());
                MongoCollection<Document> transactions = mongoDb
                                .getCollection(MongoDBSchema.TRANSACTIONS.getTableName());

                // 1. Check if stock exists
                Document stock = stockInventory
                                .find(Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())).first();
                if (stock == null) {
                        logger.info("stock not found");
                        return false;
                }

                // Check if txn is in transactions or staged
                Document inStaged = stageStockInventory
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                Document inTransactions = transactions
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                if (inStaged != null || inTransactions != null) {
                        return true;
                }

                // 2. Deduct stock from the inventory
                try {
                        stockInventory.updateOne(Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker()),
                                        Updates.inc(MongoDBSchema.KeyNames.QUANTITY.getKeyName(),
                                                        -tx.getStockAmount()));
                } catch (MongoWriteException e) {
                        logger.info("Insufficient stock. FAIL\n");
                        return false;
                }
                // 3. stage transaction in stageStockInventory
                Document stagedTXN = new Document(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID())
                                .append(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())
                                .append(MongoDBSchema.KeyNames.QUANTITY.getKeyName(), tx.getStockAmount())
                                .append(MongoDBSchema.KeyNames.BUY_SELL.getKeyName(), MongoDBSchema.KeyNames.BUY)
                                .append(MongoDBSchema.KeyNames.TIMESTAMP.getKeyName(), new Date())
                                .append(MongoDBSchema.KeyNames.LAST_UPDATED.getKeyName(), new Date());

                stageStockInventory.insertOne(stagedTXN);
                return true;
        }

        @Override
        protected boolean stageClientBuy(TransactionDetails tx) {
                MongoCollection<Document> clientPortfolio = mongoDb
                                .getCollection(MongoDBSchema.CLIENT_PORTFOLIO.getTableName());
                MongoCollection<Document> stageClientPortfolio = mongoDb
                                .getCollection(MongoDBSchema.STAGE_CLIENT_PORTFOLIO.getTableName());
                MongoCollection<Document> transactions = mongoDb
                                .getCollection(MongoDBSchema.TRANSACTIONS.getTableName());

                // 1. check if client exists (left in for two separate errors)
                Document client = clientPortfolio
                                .find(Filters.eq(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(), tx.getClientID()))
                                .first();
                if (client == null) {
                        logger.info("client not found");
                        return false;
                }

                // Check if txn is in transactions or staged
                Document inStaged = stageClientPortfolio
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                Document inTransactions = transactions
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                if (inStaged != null || inTransactions != null) {
                        return true;
                }

                try {
                        // remove the money from clients account
                        clientPortfolio.updateOne(Filters.and(
                                        Filters.eq(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(), tx.getClientID()),
                                        Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(),
                                                        MongoDBSchema.KeyNames.CASH)),
                                        Updates.inc(MongoDBSchema.KeyNames.QUANTITY.getKeyName(),
                                                        -tx.getDollarValue()));
                } catch (MongoWriteException e) {
                        logger.info("Insufficient funds. FAIL\n");
                        return false;
                }

                Document stagedTXN = new Document(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID())
                                .append(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(), tx.getClientID())
                                .append(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())
                                .append(MongoDBSchema.KeyNames.QUANTITY.getKeyName(), tx.getStockAmount())
                                .append(MongoDBSchema.KeyNames.BUY_SELL.getKeyName(), MongoDBSchema.KeyNames.BUY)
                                .append(MongoDBSchema.KeyNames.PRICE.getKeyName(), tx.getDollarValue())
                                .append(MongoDBSchema.KeyNames.TIMESTAMP.getKeyName(), new Date())
                                .append(MongoDBSchema.KeyNames.LAST_UPDATED.getKeyName(), new Date());

                stageClientPortfolio.insertOne(stagedTXN);
                return true;
        }

        @Override
        protected void commitBrokerBuy(TransactionDetails tx) {
                MongoCollection<Document> stageStockInventory = mongoDb
                                .getCollection(MongoDBSchema.STAGE_STOCK_INVENTORY.getTableName());

                // 1. Check if txn in staged
                Document txn = stageStockInventory
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                if (txn == null) {
                        logger.warning("TXN not in staged");
                }
                // remove from staged table, it has been completed
                try {
                        stageStockInventory.deleteOne(
                                        Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()));
                } catch (MongoWriteException e) {
                        logger.severe("error removing tx from staged");
                }
        }

        @Override
        protected void commitClientBuy(TransactionDetails tx) {
                MongoCollection<Document> stageClientPortfolio = mongoDb
                                .getCollection(MongoDBSchema.STAGE_CLIENT_PORTFOLIO.getTableName());
                MongoCollection<Document> clientPortfolio = mongoDb
                                .getCollection(MongoDBSchema.CLIENT_PORTFOLIO.getTableName());
                MongoCollection<Document> transactions = mongoDb
                                .getCollection(MongoDBSchema.TRANSACTIONS.getTableName());
                // check if txn in staged
                Document txn = stageClientPortfolio
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                if (txn == null) {
                        logger.warning("TXN not in staged");
                        throw new IllegalArgumentException("TXN not in staged");
                }

                // remove from staged
                try {
                        stageClientPortfolio.deleteOne(
                                        Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()));
                } catch (MongoWriteException e) {
                        logger.warning("ERROR removing from staged");
                }

                // add stock to clientPortfolio,
                try {
                        // Assuming clientPortfolio is already defined and initialized
                        // MongoCollection<Document>
                        UpdateResult result = clientPortfolio.updateOne(Filters.and(
                                        Filters.eq(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(), tx.getClientID()),
                                        Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())),
                                        Updates.combine(Updates.inc(MongoDBSchema.KeyNames.QUANTITY.getKeyName(),
                                                        tx.getStockAmount()), Updates.set("lastUpdated", new Date()), // Set
                                                                                                                      // the
                                                                                                                      // last
                                                                                                                      // updated
                                                                                                                      // time
                                                                                                                      // to
                                                                                                                      // the
                                                                                                                      // current
                                                                                                                      // date
                                                                                                                      // and
                                                                                                                      // time
                                                        Updates.set("timestamp", new Date())));

                        // Insert a new row if necessary
                        if (result.getModifiedCount() == 0) { // Check if no documents were updated
                                Document row = new Document(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(),
                                                tx.getClientID())
                                                                .append(MongoDBSchema.KeyNames.TICKER.getKeyName(),
                                                                                tx.getTicker())
                                                                .append(MongoDBSchema.KeyNames.QUANTITY.getKeyName(),
                                                                                tx.getStockAmount())
                                                                .append("lastUpdated", new Date())
                                                                .append("timestamp", new Date()); // Add a last updated
                                                                                                  // field with the
                                                                                                  // current date and
                                                                                                  // time
                                clientPortfolio.insertOne(row);
                        }
                } catch (MongoWriteException e) {
                        logger.info("error adding the stock to clientPortfolio");
                }

                postToKafka(tx);

                // Also add this tx to the tx table
                Document transaction = new Document(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID())
                                .append(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(), tx.getClientID())
                                .append(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())
                                .append(MongoDBSchema.KeyNames.QUANTITY.getKeyName(), tx.getStockAmount())
                                .append(MongoDBSchema.KeyNames.BUY_SELL.getKeyName(), MongoDBSchema.KeyNames.BUY)
                                .append(MongoDBSchema.KeyNames.PRICE.getKeyName(), tx.getDollarValue())
                                .append(MongoDBSchema.KeyNames.TIMESTAMP.getKeyName(), new Date())
                                .append(MongoDBSchema.KeyNames.LAST_UPDATED.getKeyName(), new Date());
                transactions.insertOne(transaction);
        }

        @Override
        protected boolean stageBrokerSell(TransactionDetails tx) {
                MongoCollection<Document> stockInventory = mongoDb
                                .getCollection(MongoDBSchema.STOCK_INVENTORY.getTableName());
                MongoCollection<Document> stageStockInventory = mongoDb
                                .getCollection(MongoDBSchema.STAGE_STOCK_INVENTORY.getTableName());
                MongoCollection<Document> transactions = mongoDb
                                .getCollection(MongoDBSchema.TRANSACTIONS.getTableName());
                // check stock exists
                Document stock = stockInventory
                                .find(Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())).first();
                if (stock == null) {
                        logger.warning("Stock not found");
                        return false;
                }

                // Check if txn is in transactions or staged
                Document inStaged = stageStockInventory
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                Document inTransactions = transactions
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                if (inStaged != null || inTransactions != null) {
                        return true;
                }

                // ensure there is enough stock
                if ((Integer) stock.get(MongoDBSchema.KeyNames.QUANTITY.getKeyName()) < tx.getStockAmount()) {
                        return false;
                }

                // add to staging table
                Document txn = new Document(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID())
                                .append(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())
                                .append(MongoDBSchema.KeyNames.QUANTITY.getKeyName(), tx.getStockAmount())
                                .append(MongoDBSchema.KeyNames.BUY_SELL.getKeyName(), MongoDBSchema.KeyNames.SELL)
                                .append(MongoDBSchema.KeyNames.TIMESTAMP.getKeyName(), new Date())
                                .append(MongoDBSchema.KeyNames.LAST_UPDATED.getKeyName(), new Date());
                stageStockInventory.insertOne(txn);
                return true;
        }

        @Override
        protected boolean stageClientSell(TransactionDetails tx) {
                MongoCollection<Document> stageClientPortfolio = mongoDb
                                .getCollection(MongoDBSchema.STAGE_CLIENT_PORTFOLIO.getTableName());
                MongoCollection<Document> clientPortfolio = mongoDb
                                .getCollection(MongoDBSchema.CLIENT_PORTFOLIO.getTableName());
                MongoCollection<Document> transactions = mongoDb
                                .getCollection(MongoDBSchema.TRANSACTIONS.getTableName());

                // Check if txn is in transactions or staged
                Document inStaged = stageClientPortfolio
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                Document inTransactions = transactions
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                if (inStaged != null || inTransactions != null) {
                        return true;
                }

                // check if client exists and if client owns stock
                Document client_ticker = clientPortfolio
                                .find(Filters.and(
                                                Filters.eq(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(),
                                                                tx.getClientID()),
                                                Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())))
                                .first();
                if (client_ticker == null) {
                        return false;
                }

                // remove from clientPortfolio, autofails if any issues
                try {
                    clientPortfolio.updateOne(Filters.and(
                        Filters.eq(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(), tx.getClientID()),
                        Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())),
                    Updates.inc(MongoDBSchema.KeyNames.QUANTITY.getKeyName(), -tx.getStockAmount()));
                } catch (MongoWriteException e) {
                        logger.info("Insufficient stock owned. FAIL\n");
                        return false;
                }

                // add to staging
                Document txn = new Document(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID())
                                .append(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(), tx.getClientID())
                                .append(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())
                                .append(MongoDBSchema.KeyNames.QUANTITY.getKeyName(), tx.getStockAmount())
                                .append(MongoDBSchema.KeyNames.BUY_SELL.getKeyName(), MongoDBSchema.KeyNames.BUY)
                                .append(MongoDBSchema.KeyNames.PRICE.getKeyName(), tx.getDollarValue())
                                .append(MongoDBSchema.KeyNames.TIMESTAMP.getKeyName(), new Date())
                                .append(MongoDBSchema.KeyNames.LAST_UPDATED.getKeyName(), new Date());
                stageClientPortfolio.insertOne(txn);

                return true;
        }

        @Override
        protected void commitBrokerSell(TransactionDetails tx) {
                MongoCollection<Document> stageStockInventory = mongoDb
                                .getCollection(MongoDBSchema.STAGE_STOCK_INVENTORY.getTableName());
                MongoCollection<Document> stockInventory = mongoDb
                                .getCollection(MongoDBSchema.STOCK_INVENTORY.getTableName());

                // 1. check if txn exists
                Document txn = stageStockInventory
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                if (txn == null) {
                        logger.warning("TXN not in staged");
                }

                // increase stock
                try {
                        stockInventory.updateOne(Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker()),
                                        Updates.inc(MongoDBSchema.KeyNames.QUANTITY.getKeyName(), tx.getStockAmount()));
                } catch (MongoWriteException e) {
                        e.printStackTrace();
                        logger.info("howd you get here?");
                        // Cant fail but do it anyways
                }

                // remove from staged
                stageStockInventory.deleteOne(
                                Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()));
        }

        @Override
        protected void commitClientSell(TransactionDetails tx) {
                MongoCollection<Document> transactions = mongoDb
                                .getCollection(MongoDBSchema.TRANSACTIONS.getTableName());
                MongoCollection<Document> stageClientPortfolio = mongoDb
                                .getCollection(MongoDBSchema.STAGE_CLIENT_PORTFOLIO.getTableName());
                MongoCollection<Document> clientPortfolio = mongoDb
                                .getCollection(MongoDBSchema.CLIENT_PORTFOLIO.getTableName());
                // check txn in staged

                // 1. check if txn exists
                Document txn = stageClientPortfolio
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                if (txn == null) {
                        logger.warning("TXN not in staged");
                }

                // 2. Update client's portfolio to add money
                try {
                    UpdateResult result = clientPortfolio.updateOne(Filters.and(
                            Filters.eq(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(), tx.getClientID()),
                            Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())),
                        Updates.combine(
                            Updates.inc(MongoDBSchema.KeyNames.QUANTITY.getKeyName(), tx.getStockAmount()),
                            Updates.set(MongoDBSchema.KeyNames.LAST_UPDATED.getKeyName(), new Date()), // Update lastUpdated
                            Updates.set(MongoDBSchema.KeyNames.TIMESTAMP.getKeyName(), new Date()) // Ensure timestamp is updated here
                        ));
            
                    if (result.getModifiedCount() == 0) {
                        Document insertion = new Document(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(), tx.getClientID())
                            .append(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())
                            .append(MongoDBSchema.KeyNames.QUANTITY.getKeyName(), tx.getStockAmount())
                            .append(MongoDBSchema.KeyNames.LAST_UPDATED.getKeyName(), new Date())
                            .append(MongoDBSchema.KeyNames.TIMESTAMP.getKeyName(), new Date());
                        clientPortfolio.insertOne(insertion);
                    }
                } catch (MongoWriteException e) {
                    logger.info("Error adding the stock to clientPortfolio");
                }

                // 3. remove from staged
                try {
                        stageClientPortfolio.deleteOne(
                                        Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()));
                } catch (MongoWriteException e) {
                        logger.info("Error deleting from stageClient");
                }

                postToKafka(tx);

                // Also add tx to tx table
                Document transaction = new Document(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID())
                                .append(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(), tx.getClientID())
                                .append(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker())
                                .append(MongoDBSchema.KeyNames.QUANTITY.getKeyName(), tx.getStockAmount())
                                .append(MongoDBSchema.KeyNames.BUY_SELL.getKeyName(), MongoDBSchema.KeyNames.SELL)
                                .append(MongoDBSchema.KeyNames.PRICE.getKeyName(), tx.getDollarValue())
                                .append(MongoDBSchema.KeyNames.TIMESTAMP.getKeyName(), new Date())
                                .append(MongoDBSchema.KeyNames.LAST_UPDATED.getKeyName(), new Date());
                transactions.insertOne(transaction);

        }

        @Override
        protected void undoBrokerStaging(TransactionDetails tx) {
                MongoCollection<Document> stageStockInventory = mongoDb
                                .getCollection(MongoDBSchema.STAGE_STOCK_INVENTORY.getTableName());
                MongoCollection<Document> stockInventory = mongoDb
                                .getCollection(MongoDBSchema.STOCK_INVENTORY.getTableName());

                // check if txn in this stageing table, if not, need not do anything
                Document txn = stageStockInventory
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                // remove from stage table if present
                if (txn != null) {
                        stageStockInventory.deleteOne(
                                        Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()));
                        // if buy, add stock back into circulation
                        if (tx.getTransactionType() == TransactionDetails.TransactionType.BUY) {
                                stockInventory.updateOne(
                                                Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker()),
                                                Updates.inc(MongoDBSchema.KeyNames.QUANTITY.getKeyName(),
                                                                tx.getStockAmount()));
                        }
                }
        }

        @Override
        protected void undoClientStaging(TransactionDetails tx) {
                MongoCollection<Document> transactions = mongoDb
                                .getCollection(MongoDBSchema.TRANSACTIONS.getTableName());
                MongoCollection<Document> stageClientPortfolio = mongoDb
                                .getCollection(MongoDBSchema.STAGE_CLIENT_PORTFOLIO.getTableName());
                MongoCollection<Document> clientPortfolio = mongoDb
                                .getCollection(MongoDBSchema.CLIENT_PORTFOLIO.getTableName());

                // check if txn in staged
                Document txn = stageClientPortfolio
                                .find(Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()))
                                .first();
                // remove from stage table if present
                if (txn != null) {
                        stageClientPortfolio.deleteOne(
                                        Filters.eq(MongoDBSchema.KeyNames.TXN_ID.getKeyName(), tx.getTransactionID()));
                        // if buy, add stock back into circulation
                        if (tx.getTransactionType() == TransactionDetails.TransactionType.BUY) {
                                clientPortfolio.updateOne(
                                                Filters.and(Filters.eq(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(),
                                                                tx.getClientID()),
                                                                Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(),
                                                                                MongoDBSchema.KeyNames.CASH)),
                                                Updates.inc(MongoDBSchema.KeyNames.QUANTITY.getKeyName(),
                                                                +tx.getDollarValue()));
                        } else {
                                // if sell, add stock back
                                clientPortfolio.updateOne(Filters.and(
                                                Filters.eq(MongoDBSchema.KeyNames.TICKER.getKeyName(), tx.getTicker()),
                                                Filters.eq(MongoDBSchema.KeyNames.CLIENT_ID.getKeyName(),
                                                                tx.getClientID())),
                                                Updates.inc(MongoDBSchema.KeyNames.QUANTITY.getKeyName(),
                                                                +tx.getStockAmount()));
                        }
                }
        }

        private static void printCollection(MongoDatabase database, String collectionName) {
                // Get the collection
                MongoCollection<Document> collection = database.getCollection(collectionName);

                // Print out the collection name
                System.out.println("Collection: " + collectionName);

                // Find all documents in the collection
                MongoIterable<Document> documents = collection.find();

                // Iterate through the documents and print them out
                for (Document doc : documents) {
                        System.out.println(doc.toJson());
                }
        }

        public void printDB(String collectionName) {

                // Print out 'transactions' collection
                printCollection(mongoDb, collectionName);
        }

        /**
         * Commits a new Client based on the provided TX, Implements all logic required
         * for a commit, including ensuring idempotency
         *
         * @param tx
         */
        public void commitNewClient(TransactionDetails tx) {

                // db.clientPortfolio.insertOne({ clientID: "exampleClientId", stockID:
                // "exampleStockId", quantityOwned: 11, timestamp: new Date(), lastUpdated: new
                // Date() });
                // Get the clientPortfolio collection
                MongoCollection<Document> collection = mongoDb.getCollection("clientPortfolio");

                // Create a new Document object with the data from the TransactionDetails object
                Document newClient = new Document().append("clientID", tx.getClientID())
                                .append("ticker", tx.getTicker()).append("quantityOwned", 1000000000)
                                .append("timestamp", System.nanoTime()).append("lastUpdated", System.nanoTime());

                // Insert the new document into the collection
                collection.insertOne(newClient);

        }

        /**
         * posts the completed transaction to the kafka queue and returns a future
         * 
         * @param unit the TransactionUnit relevant to the post
         * @return the future returned by the kafka.send to check for any exceptions
         */
        public void postToKafka(TransactionDetails tx) {
                MongoCollection<Document> clientPortfolio = mongoDb
                                .getCollection(MongoDBSchema.CLIENT_PORTFOLIO.getTableName());
                System.out.println("Posting to kafka");

                // TODO integrate kafka in
                Document clientInfo = clientPortfolio
                                .find(Filters.and(Filters.eq(KeyNames.CLIENT_ID.getKeyName(), tx.getClientID()),
                                                Filters.eq(KeyNames.TICKER.getKeyName(), tx.getTicker())))
                                .first();

                String kafkaMessage = String.format(
                                "{\"clientId\":\"%d\", \"ticker\": \"%s\", \"quantity\": %d, \"last_updated\": 4}",
                                tx.getClientID(), tx.getTicker(), clientInfo.get(KeyNames.QUANTITY.getKeyName()));

                CompletableFuture<RecordMetadata> completableFuture = new CompletableFuture<>();

                ProducerRecord<String, String> record = new ProducerRecord<>("ClientHoldingsTopic", tx.getTicker(),
                                kafkaMessage);

                kafkaProducer.send(record, (metadata, exception) -> {
                        if (exception == null) {
                                System.out.println("Message sent successfully to topic " + metadata.topic()
                                                + " partition " + metadata.partition() + " at offset "
                                                + metadata.offset());
                                completableFuture.complete(metadata);
                        } else {
                                System.out.println("Error sending message to Kafka: " + exception.getMessage());
                                completableFuture.completeExceptionally(exception);
                        }
                });

                try {
                        completableFuture.get();
                } catch (ExecutionException | InterruptedException e) {
                        // TODO add logging
                        throw new RuntimeException(e);
                }
        }

}