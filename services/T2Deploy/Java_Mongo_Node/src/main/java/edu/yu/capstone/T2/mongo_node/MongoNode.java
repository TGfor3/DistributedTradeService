package edu.yu.capstone.T2.mongo_node;

import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import com.rabbitmq.client.*;
import edu.yu.capstone.T2.mongo_node.impl.TransactionImpl;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoNode {

    private Channel rabbitChannel;

    private final RepositoryPatternBase repository_connector;
    private final String rabbitQueueName;
    private final String generalRoutingKey;
    private final String exchangeName;
    Logger logger = Logger.getLogger(TransactionImpl.class.getName());
    private final BlockingQueue<TxDetailsAndRoutingKey> messageQueue;
    private int mongoRetries = 3;


    private final ExecutorService executorService;

    public enum NodeType{
        BROKERAGE_COLLECTION, CLIENT_COLLECTION
    }

    public enum CollectionType{
        STOCK_INVENTORY, CLIENT_PORTFOLIO, TRANSACTIONS
    }

    private final NodeType nodeType;
    private final CollectionType collectionType;



    public MongoNode(ConnectionFactory rabbitBus,
                     String rabbitQueueName,
                     String generalRoutingKey,
                     String exchangeName,
                     RepositoryPatternBase repository_connector,
                     NodeType nodeType,
                     CollectionType collectionType){
        this.rabbitQueueName = rabbitQueueName;
        this.generalRoutingKey = generalRoutingKey;
        this.exchangeName = exchangeName;
        this.repository_connector = repository_connector;
        this.executorService = Executors.newCachedThreadPool();
        this.nodeType = nodeType;
        this.collectionType = collectionType;
        this.messageQueue = new ArrayBlockingQueue<>(1);
//        this.logger.setLevel(Level.SEVERE);

        int rabbitRetries = 300;
        for (int i = 0; i < rabbitRetries; i++){
            System.out.println("Trying to connect");
            try {

                Connection connection = rabbitBus.newConnection();
                this.rabbitChannel = connection.createChannel();
                // This tells RabbitMQ not to give more than one message to a worker at a time
                int prefetchCount = 1;
                rabbitChannel.basicQos(prefetchCount);
                System.out.println("Rabbit setup properly");
                break;
            } catch (IOException | TimeoutException e) {
                if (i < rabbitRetries - 1){
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Thread.interrupted();
                        logger.info("Interrupted exception in MongoNode constructor");
                        throw new RuntimeException(ex);
                    }
                } else {
                    System.out.printf("\n\n 300 retries \n\n");
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private class TxDetailsAndRoutingKey{
        private final TransactionDetails transactionDetails;
        private final String routingKey;

        TxDetailsAndRoutingKey(TransactionDetails transactionDetails, String routingKey){
            this.transactionDetails = transactionDetails;
            this.routingKey = routingKey;
        }

        public TransactionDetails getTransactionDetails() {
            return this.transactionDetails;
        }

        public String getRoutingKey() {
            return this.routingKey;
        }
    }

    public void run(){
        try {
            rabbitChannel.queueDeclare(rabbitQueueName, false, false, false, null);
        } catch (IOException e) {
            logger.severe("Error declaring queue rabbit queue");
            throw new RuntimeException(e);
        }
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
//            System.out.println("Inside get callback");
            //strip generalRoutingKey so only have orchestrator specific routingKey to return with
            String routingKey = delivery.getEnvelope().getRoutingKey();
//            System.out.println("1");
            String orchestratorKey = routingKey.substring(generalRoutingKey.length() + 1);
//            System.out.println("2");
            TxDetailsAndRoutingKey detailsAndRoutingKey = new TxDetailsAndRoutingKey(
                    TransactionDetails.deserialize(delivery.getBody()),
                    orchestratorKey
            );
            System.out.printf("Received tx from orchestrator: %s\n", orchestratorKey);
            System.out.println(TransactionDetails.deserialize(delivery.getBody()));
            try {
//                System.out.println("trying to placeo n the queue");
                messageQueue.put(detailsAndRoutingKey);
//                System.out.println("acking message");
                rabbitChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (InterruptedException e) {
                System.out.println("Error");
                rabbitChannel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                logger.info(String.format("Error placing message on blockingQueue txnID: %d\n", TransactionDetails.deserialize(delivery.getBody()).getTransactionID()));
            }
        };


        try {
            //spawns a thread on the side to listen for messages
            rabbitChannel.basicConsume(rabbitQueueName, false, deliverCallback, consumerTag -> {}); //true here for auto ack
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        switch (this.nodeType) {
            case BROKERAGE_COLLECTION -> runAsBrokerInventory();
            case CLIENT_COLLECTION -> runAsClientInventory();
        }
    }

    /**
     * returns provided details to the queue
     * @param detailsAndRoutingKey the TxDetails object to be returned to the queue
     */
    private void returnToQueue(TxDetailsAndRoutingKey detailsAndRoutingKey){
        try {
            //receiveQueue is orchestrator is recieving from Mongo/Worker Nodes
            System.out.printf("returning tx %d to queue %s. Result is: %s\n", detailsAndRoutingKey.transactionDetails.getTransactionID(), detailsAndRoutingKey.routingKey, detailsAndRoutingKey.transactionDetails.getCompletionStatus());
            rabbitChannel.basicPublish(exchangeName, detailsAndRoutingKey.routingKey, null, detailsAndRoutingKey.getTransactionDetails().serialize());
//            System.out.printf("published to routingKey: %s\n", detailsAndRoutingKey.routingKey);
        } catch (Exception e) {
            logger.info("Error with returning to queue, something with rabbit \n");
            Thread.interrupted();
            e.printStackTrace();
        }
    }

    private void runAsBrokerInventory(){
        while (true){
            TxDetailsAndRoutingKey detailsAndRoutingKey = null;
            try {
                detailsAndRoutingKey = messageQueue.take();
            } catch (InterruptedException e) {
                logger.severe("Error retrieving message from messageQueue");
                throw new RuntimeException(e);
            }

            TransactionDetails.CompletionType result;
            TransactionDetails details = detailsAndRoutingKey.transactionDetails;
            switch (detailsAndRoutingKey.getTransactionDetails().getCompletionStatus()) {
                case UNFINISHED -> {
                    boolean success = false;
//                    for (int i = 0; i < mongoRetries || success;i++){
                        try {
                            if (details.getTransactionType() == TransactionDetails.TransactionType.BUY){
                                success = repository_connector.stageBrokerBuy(details);
                            } else {
                                success = repository_connector.stageBrokerSell(details);
                            }
                        } catch (MongoTimeoutException e){
                            System.out.println("Reconnecting");
                            repository_connector.connect();
                        }
//                    }

                    if (success)
                        result = TransactionDetails.CompletionType.SUCCESS;
                    else
                        result = TransactionDetails.CompletionType.FAILURE;
                }
                case FAILURE -> {
                    result = TransactionDetails.CompletionType.RECOVERED;
                    for (int i = 0; i < mongoRetries;i++){
                        try {
                            repository_connector.undoBrokerStaging(details);
                            break;
                        } catch (MongoTimeoutException e){
                            System.out.println("Reconnecting");
                            repository_connector.connect();
                        }
                    }
                }
                case SUCCESS -> {
                    result = TransactionDetails.CompletionType.COMMITTED;
//                    for (int i = 0; i < mongoRetries;i++){
                        try {
                            if (details.getTransactionType() == TransactionDetails.TransactionType.BUY){
                                repository_connector.commitBrokerBuy(details);
                            } else {
                                repository_connector.commitBrokerSell(details);
                            }
                            break;
                        } catch (MongoTimeoutException e){
                            System.out.println("Reconnecting");
                            repository_connector.connect();
                        }
//                    }

                }
                default -> {continue;}
            }
            detailsAndRoutingKey.transactionDetails.setCompletionStatus(result);
            returnToQueue(detailsAndRoutingKey);
        }
    }

    private void runAsClientInventory(){
        while (true){
            TxDetailsAndRoutingKey detailsAndRoutingKey = null;
            try {
                detailsAndRoutingKey = messageQueue.take();
            } catch (InterruptedException e) {
                logger.severe("Error retrieving message from messageQueue");
                throw new RuntimeException(e);
            }

            TransactionDetails.CompletionType result;
            TransactionDetails details = detailsAndRoutingKey.transactionDetails;
            switch (detailsAndRoutingKey.getTransactionDetails().getCompletionStatus()) {
                case UNFINISHED -> {
                    boolean success = false;
//                    for (int i = 0; i < mongoRetries || success;i++){
                        try {
                            if (details.getTransactionType() == TransactionDetails.TransactionType.BUY){
                                success = repository_connector.stageClientBuy(details);
                            } else {
                                success = repository_connector.stageClientSell(details);
                            }
                        } catch (MongoTimeoutException e){
                            System.out.println("Reconnecting");
                            repository_connector.connect();
                        }
//                    }

                    if (success)
                        result = TransactionDetails.CompletionType.SUCCESS;
                    else
                        result = TransactionDetails.CompletionType.FAILURE;
                }
                case FAILURE -> {
                    result = TransactionDetails.CompletionType.RECOVERED;
                    for (int i = 0; i < mongoRetries;i++){
                        try {
                            repository_connector.undoClientStaging(details);
                            break;
                        } catch (MongoTimeoutException e){
                            System.out.println("Reconnecting");
                            repository_connector.connect();
                        }
                    }
                }
                case SUCCESS -> {
                    result = TransactionDetails.CompletionType.COMMITTED;
//                    for (int i = 0; i < mongoRetries;i++){
                        try {
                            if (details.getTransactionType() == TransactionDetails.TransactionType.BUY){
                                repository_connector.commitClientBuy(details);
                            } else {
                                repository_connector.commitClientSell(details);
                            }
                            break;
                        } catch (MongoTimeoutException e){
                            System.out.println("Reconnecting");
                            repository_connector.connect();
//                        }
                    }

                }
                default -> {continue;}
            }
            detailsAndRoutingKey.transactionDetails.setCompletionStatus(result);
            returnToQueue(detailsAndRoutingKey);
        }
    }

}