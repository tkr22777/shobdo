package common.stores;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.ConfigFactory;
import utilities.ShobdoLogger;

/* A central factory that returns word and request collection */
public class MongoStoreFactory {

    private static final ShobdoLogger log = new ShobdoLogger(MongoStoreFactory.class);

    /* MONGODB HOST INFO */
    private static final String HOSTNAME = ConfigFactory.load().getString("shobdo.mongodb.hostname");
    private static final int PORT = Integer.parseInt(ConfigFactory.load().getString("shobdo.mongodb.port"));

    /* MONGODB INFO */
    private static final String DB_NAME = ConfigFactory.load().getString("shobdo.mongodb.database.dbname");

    /* MONGODB SINGLETON CLIENT */
    private static MongoDatabase mongoDB;

    /* MONGODB COLLECTIONS */
    private static final String COLLECTION_WORDS = ConfigFactory.load().getString("shobdo.mongodb.database.collection.words");
    private static final String COLLECTION_REQUESTS = ConfigFactory.load().getString("shobdo.mongodb.database.collection.userrequests");

    private static MongoCollection wordCollection;
    private static MongoCollection userRequestsCollection;

    private MongoStoreFactory() {
    }

    private static synchronized MongoDatabase getDatabase() {
        if (mongoDB == null) {
            try {
                log.info("@MM001 Connecting to mongodb [host:" + HOSTNAME + "][port:" + PORT + "]");
                MongoClientOptions options = MongoClientOptions.builder()
                    .socketTimeout(3000)
                    .connectTimeout(3000)
                    .build();
                MongoClient client = new MongoClient(new ServerAddress(HOSTNAME, PORT), options);
                mongoDB = client.getDatabase(DB_NAME);
            } catch (Exception ex) {
                throw new RuntimeException(String.format("Could not connect to MongoDB host at: %s", HOSTNAME));
            }
        }
        return mongoDB;
    }

    public static synchronized MongoCollection getWordCollection() {
        if (wordCollection == null) {
            wordCollection = getDatabase().getCollection(COLLECTION_WORDS);
        }
        return wordCollection;
    }

    public static synchronized MongoCollection getUserRequestsCollection() {
        if (userRequestsCollection == null) {
            userRequestsCollection = getDatabase().getCollection(COLLECTION_REQUESTS);
        }
        return userRequestsCollection;
    }
}
