package edu.yu.capstone.T2.backend;

import java.io.*;

public class TransactionDetails {

    public enum TransactionType {
        BUY, SELL
    }

    public enum CompletionType{
        SUCCESS, FAILURE, UNFINISHED, RECOVERED, COMMITTED
    }

    private int clientID;
    private String ticker;
    private int stockAmount;
    private TransactionType transactionType;
    private CompletionType completionStatus;
    private int dollarValue;
    private long transactionID;
    private int lockinPrice;

    /**
     * Basic constructor
     * @param clientID id of the client being transacted upon
     * @param ticker id of the stock being transacted upon
     * @param stockAmount units of stock being transferred
     * @param transactionType enum of the type of transaciton occuring
     * @param lockinPrice individual price of each unit at lockin time
     * @param transactionID txnID of the transaction
     * @param completionStatus the status of the transaction
     */
    public TransactionDetails(int clientID, String ticker, int stockAmount, TransactionType transactionType, int lockinPrice, long transactionID, CompletionType completionStatus){
        this.clientID = clientID;
        this.ticker = ticker;
        this.stockAmount = stockAmount;
        this.transactionType = transactionType;
        this.dollarValue = lockinPrice * stockAmount;
        this.transactionID = transactionID;
        this.lockinPrice = lockinPrice;
        this.completionStatus = completionStatus;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeInt(clientID);
        dos.writeInt(ticker.length());
        dos.writeBytes(ticker);
        dos.writeInt(stockAmount);
        dos.writeInt(transactionType.ordinal());
        dos.writeInt(lockinPrice);
        dos.writeLong(transactionID);
        dos.writeInt(completionStatus.ordinal());

        dos.flush();
        return baos.toByteArray();
    }

    public static TransactionDetails deserialize(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);

        int clientID = dis.readInt();
        int tickerLength = dis.readInt();
        byte[] tickerBytes = new byte[tickerLength];
        dis.readFully(tickerBytes);
        String ticker = new String(tickerBytes);

        int stockAmount = dis.readInt();
        TransactionType transactionType = TransactionType.values()[dis.readInt()];
        int lockinPrice = dis.readInt();
        long transactionID = dis.readLong();
        CompletionType completionStatus = CompletionType.values()[dis.readInt()];

        return new TransactionDetails(clientID, ticker, stockAmount, transactionType, lockinPrice, transactionID, completionStatus);
    }


    // Getters
    public int getClientID() {
        return clientID;
    }
    public String getTicker() {
        return ticker;
    }
    public int getStockAmount() {
        return stockAmount;
    }
    public int getLockinPrice() {return lockinPrice;}
    public TransactionType getTransactionType() {
        return transactionType;
    }
    public int getDollarValue() {
        return dollarValue;
    }
    public long getTransactionID() {
        return transactionID;
    }
    public void setCompletionStatus(CompletionType completionStatus){
        this.completionStatus = completionStatus;
    }

    public CompletionType getCompletionStatus(){return completionStatus;}

    @Override
    public String toString() {
        return "TransactionDetails{" +
                "clientID=" + clientID +
                ", ticker='" + ticker + '\'' +
                ", stockAmount=" + stockAmount +
                ", transactionType=" + transactionType +
                ", completionStatus=" + completionStatus +
                ", dollarValue=" + dollarValue +
                ", transactionID=" + transactionID +
                ", lockinPrice=" + lockinPrice +
                '}';
    }

}
