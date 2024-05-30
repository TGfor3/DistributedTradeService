package edu.yu.capstone.T2.backend.TimerUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class TimerClass implements Runnable{

    final CountDownLatch latch;
    private long awaitTime;
    private TimeUnit timeUnit;
    private long txnID;
    public TimerClass(long txnID, CountDownLatch latch, long awaitTime, TimeUnit timeUnit){
        this.latch = latch;
        this.awaitTime = awaitTime;
        this.timeUnit = timeUnit;
        this.txnID = txnID;
    }

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            boolean completed = latch.await(awaitTime, timeUnit);
            if (!completed)
                throw new TimerError(String.valueOf(txnID));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
