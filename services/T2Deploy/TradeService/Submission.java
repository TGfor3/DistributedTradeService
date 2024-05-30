//package edu.yu.capstone.T2.frontend;
//Fix this ^
/**
 * A basic Submission API to our system
 *      you will have a transactionID returned to keep track of the output to the Queue of Truth
 */
public interface Submission{
    long submitBuy(int clientID, String ticker, int buyAmount);
    long submitSell(int clientID, String ticker, int sellAmount);
    long register(int clientID, String startingBalance);
}
