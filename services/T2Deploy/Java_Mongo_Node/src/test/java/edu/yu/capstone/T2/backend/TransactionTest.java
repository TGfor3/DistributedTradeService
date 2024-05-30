package edu.yu.capstone.T2.backend;

import edu.yu.capstone.T2.mongo_node.TransactionDetails;
import edu.yu.capstone.T2.mongo_node.impl.TransactionImpl;
import org.junit.jupiter.api.Test;

public class TransactionTest {

    String uriString = "mongodb://isigutt:isi@localhost:27017/admin?retryWrites=false";

    @Test
    public void buyAStock(){
        //
//        TransactionImpl tx = new TransactionImpl(uriString);
//
//        System.out.println("DB on start up: ");
//
//
//
//        //Add new client with 100$ of CASH
//        System.out.println("Adding a new user with 1000$ in CASH");
//
//        TransactionDetails client = new TransactionDetails(1, "CASH", 10001, TransactionDetails.TransactionType.BUY, 1000, 1, null);
//       //tx.commitNewClient(client);
//
//        System.out.println("\nDB after adding the client\n");
//
//        System.out.println("\nUser buys 10 shares of google at price 20$");
//
//        //Client buys 10 shares of google stock
//        TransactionDetails firstBuy = new TransactionDetails(1, "GOOGL", 10001, TransactionDetails.TransactionType.BUY, 1,2, null);
//        tx.(firstBuy);
//        System.out.println("\nDB after buying 10 shares of google\n");
//
//        System.out.println("\nUser sells his 10 shares of google at price 22$");
//
//        //Client sells the 10 shares of google stock
////        TransactionDetails firstSell = new TransactionDetails(1, "GOOGL", 1, TransactionDetails.TransactionType.SELL, 22, 3, null);
////        tx.commitSell(firstSell);
////        System.out.println("\nDb after selling 10 shares of google\n");

    }
}
