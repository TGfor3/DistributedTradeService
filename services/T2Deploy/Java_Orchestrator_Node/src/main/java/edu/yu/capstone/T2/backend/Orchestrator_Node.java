package edu.yu.capstone.T2.backend;

import com.rabbitmq.client.*;
import edu.yu.capstone.T2.backend.TimerUtils.TimerClass;
import edu.yu.capstone.T2.backend.TimerUtils.TimerError;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import edu.yu.capstone.T2.observe.EventManager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/*
    TODO make each individual transaction management handled by a thread
 */

public class Orchestrator_Node {
    private final ConnectionFactory rabbitBus;
    private Channel nodeChannel; // 1 channel per thread, so during rework, will have to switch this
    private Channel tradeChannel;
    private Connection connection; // Only needs 1 connection
    private final String tradeServiceQueue;
    private final String clientAccQueue;
    private final String brokerInvQueue;
    private String orchestratorReturnQueue;
    private final ExecutorService timerExecutor;
    private final ExecutorService transactionExecutor;
    private final HashMap<Long, TransactionUnit> transactionsMap;
    private final List<Future<?>> timers;
    private LinkedBlockingQueue<DeliveredTuple> waitingMessages;
    private final long awaitTime;
    private boolean shutdown = false;
    public String orchestratorKey;
    private final String exchangeName;
    private final String generalRoutingKey;
    // private final MockKafka mockKafka;
    private final EventManager eventManager;

    private class DeliveredTuple {
        private final TransactionDetails details;
        private final long deliveryTag;

        public DeliveredTuple(TransactionDetails details, long deliveryTag) {
            this.details = details;
            this.deliveryTag = deliveryTag;
        }

        protected TransactionDetails getDetails() {
            return details;
        }

        protected long getDeliveryTag() {
            return deliveryTag;
        }
    }

    private enum EventType {
        TRANSACTION_START, TRANSACTION_SUCCESS, TRANSACTION_FAILURE
    }

    public Orchestrator_Node(ConnectionFactory rabbitBus, String tradeServiceQueue, String brokerInvQueue,
            String clientAccQueue, String generalRoutingKey, String exchangeName) {
        this.rabbitBus = rabbitBus;
        this.tradeServiceQueue = tradeServiceQueue;
        this.clientAccQueue = clientAccQueue;
        this.brokerInvQueue = brokerInvQueue;
        this.generalRoutingKey = generalRoutingKey;
        this.exchangeName = exchangeName;
        this.orchestratorKey = String.valueOf(ThreadLocalRandom.current().nextInt(0, 1000000));
        // this.mockKafka = new MockKafka();
        // Hard coded
        this.eventManager = new EventManager("192.168.8.224:9092", orchestratorKey);

        this.timerExecutor = Executors.newCachedThreadPool();
        this.transactionExecutor = Executors.newCachedThreadPool();
        this.transactionsMap = new HashMap<>();
        this.timers = new ArrayList<>();
        this.waitingMessages = new LinkedBlockingQueue<>();
        // TODO determine if awaitTime should be a cmd line arg
        this.awaitTime = 10000;

        int reconnectTime = 100;
        int retries = 300;
        for (int i = 0; i < retries; i++) {
            try {
                int prefetchCount = 100;
                this.connection = this.rabbitBus.newConnection();
                this.nodeChannel = this.connection.createChannel();
                this.tradeChannel = this.connection.createChannel();
                tradeChannel.basicQos(prefetchCount);
                System.out.printf("Orchestrator Node setup properly\n\n");
                break;
            } catch (IOException | TimeoutException e) {
                if (i < retries - 1) {
                    try {
                        Thread.sleep(reconnectTime);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }

                } else {
                    System.out.printf("\n\n 300 retries \n\n");
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void run() {
        // key should be unique per node
        this.orchestratorReturnQueue = orchestratorKey + "queue";

        // setup exchanges and queues
        try {
            nodeChannel.exchangeDeclare(exchangeName, "topic");
            nodeChannel.queueDeclare(brokerInvQueue, false, false, false, null);
            nodeChannel.queueDeclare(clientAccQueue, false, false, false, null);
            nodeChannel.queueDeclare(orchestratorReturnQueue, false, false, false, null);
            tradeChannel.queueDeclare(tradeServiceQueue, true, false, false, null);

            // This binds the both queues to all messages regardless of routing key
            // messages will be duplicated to each queue that is bound to the routing key
            nodeChannel.queueBind(brokerInvQueue, exchangeName, generalRoutingKey + ".#");
            nodeChannel.queueBind(clientAccQueue, exchangeName, generalRoutingKey + ".#");
            nodeChannel.queueBind(orchestratorReturnQueue, exchangeName, orchestratorKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DeliverCallback tradeCallBack = (consumerTag, delivery) -> {
            DeliveredTuple tuple = new DeliveredTuple(TransactionDetails.deserialize(delivery.getBody()),
                    delivery.getEnvelope().getDeliveryTag());
            System.out.printf("Message received from: %s \n", tradeServiceQueue);
            eventManager.sendEvent("T2", tuple.details.getTransactionID(), EventType.TRANSACTION_START.name(),
                    System.currentTimeMillis());
            System.out.println(tuple.details.toString());
            try {
                waitingMessages.put(tuple);
            } catch (InterruptedException e) {
                // de-ack the message and continue receiving
                tradeChannel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
                System.out.printf(
                        "error placing a message on the waitingMessages from tradeQueue, txnID: %d cancelled\n",
                        TransactionDetails.deserialize(delivery.getBody()).getTransactionID());
            }
        };
        DeliverCallback receiverCallBack = (consumerTag, delivery) -> {
            DeliveredTuple tuple = new DeliveredTuple(TransactionDetails.deserialize(delivery.getBody()),
                    delivery.getEnvelope().getDeliveryTag());
            System.out.printf("Message received from: %s \n", orchestratorReturnQueue);
            System.out.println(tuple.details.toString());
            try {
                waitingMessages.put(tuple);
            } catch (InterruptedException e) {
                // de-ack the message and continue receiving
                nodeChannel.basicCancel(consumerTag);
                System.out.printf(
                        "error placing a message on the waitingMessages from receiverQueue, txnID: %d cancelled\n",
                        TransactionDetails.deserialize(delivery.getBody()).getTransactionID());
            }
        };

        // basicConsume is a non-blocking method that spawns its own thread to
        // continuosly consume messages
        try {
            // start consuming
            tradeChannel.basicConsume(tradeServiceQueue, false, tradeCallBack, consumerTag -> {
            });
            nodeChannel.basicConsume(orchestratorReturnQueue, true, receiverCallBack, consumerTag -> {
            }); // Auto-ack true here
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // //create Runnable for processing
        Runnable txProcessing = () -> {
            while (!shutdown) {
                try {
                    DeliveredTuple deliveredTuple = waitingMessages.take();
                    TransactionDetails details = deliveredTuple.getDetails();
                    // System.out.printf("Received tx, txnID: %d, clientID: %d, %s\n",
                    // details.getTransactionID(), details.getClientID(),
                    // details.getTransactionType().toString());
                    switch (deliveredTuple.getDetails().getCompletionStatus()) {
                        // if UNFINISHED add to hashmap
                        // note that the nodes are pulling from same queue, so no need to post to nodes
                        case UNFINISHED -> {
                            handleNewTransaction(deliveredTuple);
                        }
                        // if SUCCESS, registers success in hashtable, checks if finished
                        case SUCCESS -> handleSuccess(transactionsMap.get(details.getTransactionID()));
                        // if RECOVERED, register failure
                        case FAILURE -> handleFailure(transactionsMap.get(details.getTransactionID()));
                        case RECOVERED -> handleRecovery(transactionsMap.get(details.getTransactionID()));
                        case COMMITTED -> handleCommitted(transactionsMap.get(details.getTransactionID()));
                        default -> {
                            // TODO add logging
                            System.out.println("Error getting TransactionType");
                        }
                    }
                } catch (InterruptedException e) {
                    // TODO add logging
                    throw new RuntimeException(e);
                }
            }
        };

        // txProcessing.run();
        //
        // add txProcessing to transactionExecutor
        for (int i = 0; i < 3; i++) {
            transactionExecutor.submit(txProcessing);
        }

        // create Runnable for cleanup
        Runnable cleanup = () -> {
            while (!shutdown) {
                try {
                    cleanupTimers();
                } catch (InterruptedException e) {
                    // TODO add logging
                    throw new RuntimeException(e);
                }
            }
        };
        // transactionExecutor.submit(cleanup);

        // continue running the loop
        while (!shutdown) {
            txProcessing.run();
        }
    }

    /**
     * This method should be periodically run. It goes through the list of timer
     * futures in the order they were added to the list. If Completed Normally -> no
     * action TimerError -> there was a timeout in the tx, something is wrong
     * ExceuctionException or RuntimeException -> unintended path, needs error
     * handling If not Completed -> Break out of loop as timers are added in order
     * if the current timer isn't done the next one probably isn't either
     */
    private void cleanupTimers() throws InterruptedException {
        if (!timers.isEmpty()) {
            Iterator<Future<?>> it = timers.iterator();
            while (it.hasNext()) {
                // only goes through the uncompleted list, as only really checking for timers
                // that have timed out
                Future<?> future = it.next();
                if (future.isDone()) {
                    try {
                        // Retrieve the result
                        future.get();
                    } catch (ExecutionException e) {
                        Throwable cause = e.getCause();

                        // check if the cause was a timeout
                        if (cause instanceof TimerError te) {
                            long txnID = Long.parseLong(te.getMessage());
                            System.out.printf("Handling timeout for tx: %d\n", txnID);
                            TransactionUnit unit = transactionsMap.get(txnID);
                            if (unit != null) {
                                handleTimeoutFailure(unit);
                            }

                            // not a timeout error, throw whatever other error was there
                        } else {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {

                        // TODO add logging
                        System.out.println("Cleanup was interrupted");
                        Thread.currentThread().interrupt();
                    }
                    it.remove(); // Remove the future from the list once it is processed
                } else {
                    break;
                }
            }
        } else {
            Thread.sleep(10);
        }
    }

    /**
     * confirms with the rabbit worker queue to remove the transaction from the
     * equation
     */
    private void confirmTransaction(long deliveryTag) {
        System.out.println("confirming tx");
        try {
            tradeChannel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * posts the completed transaction to the kafka queue and returns a future
     * 
     * @param unit the TransactionUnit relevant to the post
     * @return the future returned by the kafka.send to check for any exceptions
     */
    private void postToKafka(TransactionUnit unit) {
        System.out.println("Posting to kafka");
        // eventManager.sendEvent("T2", unit.getDetails().getTransactionID(),
        // EventType.TRANSACTION_END.name(), System.currentTimeMillis());

        // TODO integrate kafka in
        // TransactionDetails transaction = unit.getDetails();
        // CompletableFuture<RecordMetadata> completableFuture = new
        // CompletableFuture<>();
        //
        // ProducerRecord<Long, TransactionDetails> record = new
        // ProducerRecord<>(kafkaTopicName, transaction.getTransactionID(),
        // transaction);
        //
        // kafkaProducer.send(record, (metadata, exception) -> {
        // if (exception == null) {
        // System.out.println("Message sent successfully to topic " + metadata.topic() +
        // " partition " + metadata.partition() + " at offset " + metadata.offset());
        // completableFuture.complete(metadata);
        // } else {
        // System.out.println("Error sending message to Kafka: " +
        // exception.getMessage());
        // completableFuture.completeExceptionally(exception);
        // }
        // });
        //
        // try {
        // completableFuture.get();
        // confirmTransaction(unit.getDeliveryTag());
        // } catch (ExecutionException | InterruptedException e) {
        // //TODO add logging
        // throw new RuntimeException(e);
        // }
    }

    /**
     * submits the provided message to the queues between the orchestrator and the
     * mongoNodes
     * 
     * @param details the transactionDetails to be added to the queue
     */
    private void publishToNodeQueues(TransactionDetails details) {
        try {
            byte[] serialized = details.serialize();
            nodeChannel.basicPublish(exchangeName, generalRoutingKey + "." + orchestratorKey, null, serialized);
            System.out.println("Published to cas/bis queues");
            System.out.println(details);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * increment success internally, post to Kafka if tx successfully completed
     */
    private void handleSuccess(TransactionUnit unit) {
        unit.incrementSuccesses();
        if (unit.readyForCommit()) {
            // return success TX to queues for committing
            unit.getDetails().setCompletionStatus(TransactionDetails.CompletionType.SUCCESS);
            publishToNodeQueues(unit.getDetails());
        }
    }

    /**
     * fail transaction internally, post to both mongo node queues for undoing
     */
    private void handleFailure(TransactionUnit unit) {
        unit.failTransaction();
        unit.getDetails().setCompletionStatus(TransactionDetails.CompletionType.FAILURE);
        publishToNodeQueues(unit.getDetails());
    }

    /**
     * increments a recovery internally, if fully recovered, posts a failure
     * transaction to kafka
     */
    private void handleRecovery(TransactionUnit unit) {
        unit.incrementRollbacks();
        if (unit.completeTX()) {
            // remove transaction
            transactionsMap.remove(unit.getDetails().getTransactionID());
            postToKafka(unit);
            confirmTransaction(unit.getDeliveryTag());
            eventManager.sendEvent("T2", unit.getDetails().getTransactionID(), EventType.TRANSACTION_FAILURE.name(),
                    System.currentTimeMillis());
        }
    }

    private void handleCommitted(TransactionUnit unit) {
        unit.incrementCommits();
        if (unit.completeTX()) {
            System.out.println("Completed Successfully");
            transactionsMap.remove(unit.getDetails().getTransactionID());
            postToKafka(unit);
            eventManager.sendEvent("T2", unit.getDetails().getTransactionID(), EventType.TRANSACTION_SUCCESS.name(),
                    System.currentTimeMillis());
            confirmTransaction(unit.getDeliveryTag());
        }
    }

    private void handleNewTransaction(DeliveredTuple tuple) {
        TransactionDetails details = tuple.getDetails();
        CountDownLatch latch = new CountDownLatch(1);

        TimerClass timerClass = new TimerClass(details.getTransactionID(), latch, awaitTime, TimeUnit.MILLISECONDS);
        TransactionUnit transactionUnit = new TransactionUnit(details, latch, tuple.getDeliveryTag());

        transactionsMap.put(details.getTransactionID(), transactionUnit);

        Future<?> timer = timerExecutor.submit(timerClass);
        timers.add(timer);
        // System.out.println("Added tx: %d to timers");

        publishToNodeQueues(details);
    }

    private void handleTimeoutFailure(TransactionUnit unit) {
        unit.failTransaction();
        unit.getDetails().setCompletionStatus(TransactionDetails.CompletionType.FAILURE);
        publishToNodeQueues(unit.getDetails());
    }
}
