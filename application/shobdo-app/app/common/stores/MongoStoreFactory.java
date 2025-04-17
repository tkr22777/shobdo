package common.stores;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.ConfigFactory;
import org.bson.Document;
import utilities.ShobdoLogger;

/* A central factory that returns word and request collection */
public class MongoStoreFactory {

    private static final ShobdoLogger log = new ShobdoLogger(MongoStoreFactory.class);

    /* MONGODB CONNECTION INFO */
    private static final boolean USE_CONNECTION_STRING = ConfigFactory.load().hasPath("shobdo.mongodb.connectionString");
    private static final String CONNECTION_STRING = USE_CONNECTION_STRING ? 
            ConfigFactory.load().getString("shobdo.mongodb.connectionString") : null;
    
    /* FALLBACK TO HOST/PORT CONFIG */
    private static final String HOSTNAME = ConfigFactory.load().getString("shobdo.mongodb.hostname");
    private static final int PORT = Integer.parseInt(ConfigFactory.load().getString("shobdo.mongodb.port"));

    /* MONGODB INFO */
    private static final String DB_NAME = ConfigFactory.load().getString("shobdo.mongodb.database.dbname");

    /* MONGODB SINGLETON CLIENT */
    private static MongoDatabase mongoDB;
    private static MongoClient mongoClient;

    /* MONGODB COLLECTIONS */
    private static final String COLLECTION_WORDS = ConfigFactory.load().getString("shobdo.mongodb.database.collection.words");
    private static final String COLLECTION_REQUESTS = ConfigFactory.load().getString("shobdo.mongodb.database.collection.userrequests");

    private static MongoCollection<Document> wordCollection;
    private static MongoCollection<Document> userRequestsCollection;

    private MongoStoreFactory() {
    }

    private static synchronized MongoDatabase getDatabase() {
        if (mongoDB == null) {
            try {
                if (USE_CONNECTION_STRING) {
                    // Use connection string URI
                    log.info("@MM001 Connecting to mongodb using connection string");
                    
                    // Create ServerApi instance with version V1
                    ServerApi serverApi = ServerApi.builder()
                            .version(ServerApiVersion.V1)
                            .build();
                    
                    // Build the client settings using the connection string and ServerApi
                    MongoClientSettings settings = MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString(CONNECTION_STRING))
                            .serverApi(serverApi)
                            .build();
                    
                    // Create the new client using MongoClients factory method
                    mongoClient = MongoClients.create(settings);
                    
                    // Get database name either from connection string or default DB_NAME
                    mongoDB = mongoClient.getDatabase(DB_NAME);
                    
                    // Test the connection with a ping
                    mongoDB.runCommand(new Document("ping", 1));
                    log.info("Successfully connected to MongoDB Atlas cluster");
                } else {
                    // Fallback to host/port connection using the new driver approach
                    log.info("@MM001 Connecting to mongodb [host:" + HOSTNAME + "][port:" + PORT + "]");
                    
                    MongoClientSettings settings = MongoClientSettings.builder()
                        .applyToSocketSettings(builder -> 
                            builder.connectTimeout(3000, java.util.concurrent.TimeUnit.MILLISECONDS)
                                  .readTimeout(3000, java.util.concurrent.TimeUnit.MILLISECONDS))
                        .applyToClusterSettings(builder -> 
                            builder.hosts(java.util.Collections.singletonList(
                                new com.mongodb.ServerAddress(HOSTNAME, PORT))))
                        .build();
                    
                    mongoClient = MongoClients.create(settings);
                    mongoDB = mongoClient.getDatabase(DB_NAME);
                }
            } catch (Exception ex) {
                String connectionDetails = USE_CONNECTION_STRING ? 
                    "connection string" : 
                    String.format("host: %s, port: %d", HOSTNAME, PORT);
                log.error("@MM002 Failed to connect to MongoDB using " + connectionDetails, ex);
                throw new RuntimeException("Failed to connect to MongoDB using " + connectionDetails);
            }
        }
        return mongoDB;
    }

    public static synchronized MongoCollection<Document> getWordCollection() {
        if (wordCollection == null) {
            wordCollection = getDatabase().getCollection(COLLECTION_WORDS);
        }
        return wordCollection;
    }

    public static synchronized MongoCollection<Document> getUserRequestsCollection() {
        if (userRequestsCollection == null) {
            userRequestsCollection = getDatabase().getCollection(COLLECTION_REQUESTS);
        }
        return userRequestsCollection;
    }
}
