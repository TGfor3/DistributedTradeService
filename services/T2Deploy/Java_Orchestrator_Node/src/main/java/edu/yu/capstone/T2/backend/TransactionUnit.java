package edu.yu.capstone.T2.backend;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransactionUnit {
    private final TransactionDetails details;
    private AtomicInteger successesLogged = new AtomicInteger(0);
    private AtomicInteger rollbacksCompleted = new AtomicInteger(0);
    private AtomicInteger commitsCompleted = new AtomicInteger(0);
    private CountDownLatch countDownLatch;
    private AtomicBoolean failed = new AtomicBoolean(false);
    private AtomicBoolean completed = new AtomicBoolean(false);
    private final long deliveryTag;

    public TransactionUnit(TransactionDetails details, CountDownLatch latch, long deliveryTag) {
        this.details = details;
        this.countDownLatch = latch;
        this.deliveryTag = deliveryTag;
    }

    public TransactionDetails getDetails() {
        return details;
    }
    public long getDeliveryTag() {
        return deliveryTag;
    }
    public void failTransaction() {
        failed.set(true);
    }

    /**
     * Increments the total times this transaction has been completed
     */
    public void incrementSuccesses() {
        if (!failed.get())
            successesLogged.incrementAndGet();
    }
    /**
     * Increments the total times this transaction has been rolled back
     */
    public void incrementRollbacks() {
        rollbacksCompleted.incrementAndGet();
    }
    /**
     * increments successful commits
     */
    public void incrementCommits(){
        if (!failed.get())
            commitsCompleted.incrementAndGet();
    }

    public boolean completeTX(){
        if (completed.get())
            return true;

        if (!failed.get()){
            if (commitsCompleted.get() == 2 && successesLogged.get() == 2){
                countDownLatch.countDown();
                completed.set(true);
                System.out.printf("Tx: %d completed successfully", details.getTransactionID());
                return true;
            }
        } else {
            if (rollbacksCompleted.get() == 2){
                countDownLatch.countDown();
                completed.set(true);
                System.out.printf("Tx: %d rolledback successfully", details.getTransactionID());
                return true;
            }
        }
        return false;
    }

    public boolean readyForCommit(){
        if (!failed.get() && successesLogged.get() == 2)
            return true;
        return false;
    }







}
