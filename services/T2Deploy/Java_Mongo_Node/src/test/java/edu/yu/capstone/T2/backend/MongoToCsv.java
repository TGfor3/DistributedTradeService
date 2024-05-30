package edu.yu.capstone.T2.backend;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MongoToCsv {

    public static void main(String[] args) {
        String connectionString = "mongodb://isigutt:isi@localhost:27017";
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {

            // Get the database
            MongoDatabase database = mongoClient.getDatabase("QueryDB");
            MongoCollection<Document> collection = database.getCollection("stockInventory");
            List<Document> documents = collection.find().into(new ArrayList<>());
            writeDocumentsToCsv(documents, "output.csv");
        }
    }

    private static void writeDocumentsToCsv(List<Document> documents, String filePath) {
        try (var fw = new FileWriter(filePath); var writer = new FileWriter(filePath)) {
            for (Document doc : documents) {
                // Assuming each document is a flat structure (no nested documents or arrays)
                List<String> values = new ArrayList<>();
                for (String key : doc.keySet()) {
                    values.add(doc.get(key).toString());
                }
                writer.write(String.join(",", values) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
