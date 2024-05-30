package edu.yu.capstone.T2.mongo_node;

import com.mongodb.client.MongoDatabase;

import org.apache.kafka.clients.producer.KafkaProducer;

public abstract class RepositoryPatternBase {
    private MongoDatabase mongoDB;

    public RepositoryPatternBase(String URI, KafkaProducer<String, String> kafkaProducer) {
    }

    /**
     * Initiate this Objects connection to mongo and sets the mongoDB variable
     */
    protected abstract void connect();

    /*
     * Staging rules: Always take away from a table when adding txn to staging table
     * Never add to main tables when adding txn to staging table
     *
     * Committing rules: All issues taken care of in staging table, should never
     * have a commit issue
     *
     * ClientID ticker amount 1 CASH $100 1 AAPL 100
     *
     *
     * Only 1 DB, add brokerStagingTable clientStagingTable
     *
     * Broker Staging table: txnID ticker amount B/S
     *
     * Client Staging table: txnID ticker amount B/S 1 AAPL 10 B 1 CASH 10 B
     */

    /**
     * Attempts to place buy on broker staging table, only subtracting from main
     * Broker table If fails, adds back removed
     * 
     * @return true IFF succeed
     */
    protected abstract boolean stageBrokerBuy(TransactionDetails tx);

    /**
     * Attempts to place client buy on staging table, only subtracting from main
     * client table if fails adds back removed
     * 
     * @return true IFF succeed
     */
    protected abstract boolean stageClientBuy(TransactionDetails tx);

    /**
     * removed Buy from broker table and place in main DB adds txn to txnTable
     */
    protected abstract void commitBrokerBuy(TransactionDetails tx);

    /**
     * Removes Buy from client staging and makes alterations to both client rows in
     * main DB adds tx to txnTable
     */
    protected abstract void commitClientBuy(TransactionDetails tx);

    /**
     * Attempts to place sell on broker staging table, only subtracting from main
     * Broker table If fails, adds back removed
     * 
     * @return true IFF succeed
     */
    protected abstract boolean stageBrokerSell(TransactionDetails tx);

    /**
     * Attempts to place client sell on staging table, only subtracting from main
     * client table if fails adds back removed
     * 
     * @return true IFF succeed
     */
    protected abstract boolean stageClientSell(TransactionDetails tx);

    /**
     * Removes Buy from client staging and makes alterations to both client rows in
     * main DB
     */
    protected abstract void commitBrokerSell(TransactionDetails tx);

    /**
     * Removes Buy from client staging and makes alterations to both client rows in
     * main DB
     */
    protected abstract void commitClientSell(TransactionDetails tx);

    /**
     * adds back anything removed from the broker table to the broker table
     */
    protected abstract void undoBrokerStaging(TransactionDetails tx);

    /**
     * adds back anything removed from the client table to the client table
     */
    protected abstract void undoClientStaging(TransactionDetails tx);
}
