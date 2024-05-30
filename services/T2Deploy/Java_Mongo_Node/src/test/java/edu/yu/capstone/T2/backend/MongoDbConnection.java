package edu.yu.capstone.T2.backend;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import org.bson.Document;

public class MongoDbConnection {

    public static void main(String[] args) {
        // Replace with your credentials and host details
        String connectionString = "mongodb://isigutt:isi@localhost:27017";
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {

            // Get the database
            MongoDatabase database = mongoClient.getDatabase("QueryDB"); // assuming 'test' is the name of your database

            // Print out 'clientPortfolio' collection
            printCollection(database, "clientPortfolio");

            // Print out 'stockInventory' collection
            printCollection(database, "stockInventory");

            // Print out 'transactions' collection
            printCollection(database, "transactions");
        } catch (Exception e) {
            e.printStackTrace();
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
}