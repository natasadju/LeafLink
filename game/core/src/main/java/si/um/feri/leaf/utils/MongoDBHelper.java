package si.um.feri.leaf.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBHelper {

    private static final String CONNECTION_STRING = "mongodb+srv://leafadmin:leaf123@leafy.gnw7mw8.mongodb.net/?retryWrites=true&w=majority&appName=leafy";
    private static final String DATABASE_NAME = "leafCollection";
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    public static void connect() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(CONNECTION_STRING);
            database = mongoClient.getDatabase(DATABASE_NAME);
            System.out.println("Connected to MongoDB!");
        }
    }

    public static MongoCollection<Document> getCollection(String collectionName) {
        if (database == null) {
            throw new IllegalStateException("MongoDB is not connected. Call connect() first.");
        }
        return database.getCollection(collectionName);
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed.");
        }
    }
}
