package common.stores;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.ConfigFactory;
import utilities.ShobdoLogger;

/* A central factory that returns collection, knows how for form valid queries and convert mongo documents to logical entities */
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
    private static final String COLLECTION_RECORDS = ConfigFactory.load().getString("shobdo.mongodb.database.collection.userrequests");

    private static MongoCollection wordCollection;
    private static MongoCollection userRequestsCollection;

    private MongoStoreFactory() {
    }

    private static synchronized MongoDatabase getDatabase() {
        if (mongoDB == null) {
            log.info("@MM001 Connecting to mongodb [host:" + HOSTNAME + "][port:" + PORT + "]");
            mongoDB = new MongoClient(HOSTNAME, PORT).getDatabase(DB_NAME);
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
            userRequestsCollection = getDatabase().getCollection(COLLECTION_RECORDS);
        }
        return userRequestsCollection;
    }

}
