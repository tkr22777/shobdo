package daos;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.ConfigFactory;
import objects.EntityStatus;
import objects.UserRequest;
import objects.Word;
import org.bson.Document;
import utilities.JsonUtil;
import utilities.ShobdoLogger;

/* package private */ class MongoManager {

    private static final ShobdoLogger log = new ShobdoLogger(MongoManager.class);

    /* DB HOST INFO */
    private static final String HOSTNAME = ConfigFactory.load().getString("shobdo.mongodb.hostname");
    private static final int PORT = Integer.parseInt(ConfigFactory.load().getString("shobdo.mongodb.port"));

    /* DB */
    private static final String DB_NAME = ConfigFactory.load().getString("shobdo.mongodb.database.dbname");
    // Mongodb singleton client
    private static MongoDatabase mongoDB;

    /* Collections */
    private static MongoCollection wordCollection;
    private static MongoCollection userRequestsCollection;

    private static final String COL_WORDS    = ConfigFactory.load().getString("shobdo.mongodb.database.collection.words");
    private static final String COL_REQUESTS = ConfigFactory.load().getString("shobdo.mongodb.database.collection.userrequests");

    /* helper fields */
    public static final String ID_PARAM = "id";
    private static final String STATUS_PARAM = "status";

    private MongoManager() {
    }

    private static synchronized MongoDatabase getDatabase() {
        if (mongoDB == null) {
            log.info("@RDMI001 Connecting to mongodb [host:" + HOSTNAME + "][port:" + PORT + "]");
            mongoDB = new MongoClient(HOSTNAME, PORT).getDatabase(DB_NAME);
        }
        return mongoDB;
    }

    public static synchronized MongoCollection getWordCollection() {
        if (wordCollection == null) {
            wordCollection = getDatabase().getCollection(COL_WORDS);
        }
        return wordCollection;
    }

    public static synchronized MongoCollection getUserRequestsCollection() {
        if (userRequestsCollection == null) {
            userRequestsCollection = getDatabase().getCollection(COL_REQUESTS);
        }
        return userRequestsCollection;
    }

    public static BasicDBObject getActiveObjectQuery() {
        final BasicDBObject query = new BasicDBObject();
        query.put(MongoManager.STATUS_PARAM, EntityStatus.ACTIVE.toString());
        return query;
    }

    public static UserRequest toUserRequest(final Document doc) {
        doc.remove("_id");
        return (UserRequest) JsonUtil.jStringToObject(doc.toJson(), UserRequest.class);
    }

    public static Word toWord(final Document doc) {
        doc.remove("_id");
        return (Word) JsonUtil.jStringToObject(doc.toJson(), Word.class);
    }

    public static Document toDocument(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper().configure(MapperFeature.USE_ANNOTATIONS, false);
            return Document.parse(mapper.writeValueAsString(object));
        } catch (Exception ex) {
            throw new IllegalArgumentException("objectToDocument error. Object["
                + object.toString() + "][Ex:" + ex.getStackTrace().toString());
        }
    }
}
