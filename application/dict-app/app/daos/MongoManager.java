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

    public static final String ID_PARAM = "id";
    private static final String STATUS_PARAM = "status";

    private final static ObjectMapper objectMapper = new ObjectMapper()
        .configure(MapperFeature.USE_ANNOTATIONS, false);

    private static final String MONGODB_HOSTNAME = ConfigFactory.load().getString("shobdo.mongodb.hostname");
    private static final int MONGODB_PORT = Integer.parseInt(ConfigFactory.load().getString("shobdo.mongodb.port"));
    private static final String DB_NAME = ConfigFactory.load().getString("shobdo.mongodb.database.dbname");
    private static final String WORD_COLLECTION_NAME = ConfigFactory.load().getString("shobdo.mongodb.database.collection.words");
    private static final String USER_REQUEST_COLLECTION_NAME = ConfigFactory.load().getString("shobdo.mongodb.database.collection.userrequests");

    private static final ShobdoLogger log = new ShobdoLogger(MongoManager.class);

    private static MongoDatabase mongoDB;
    private static MongoCollection wordCollection;
    private static MongoCollection userRequestsCollection;

    private MongoManager() {
    }

    private static MongoDatabase getDatabase() {
        if (mongoDB == null) {
            log.info("@RDMI001 Connecting to mongodb [host:" + MONGODB_HOSTNAME + "][port:" + MONGODB_PORT + "]");
            mongoDB = new MongoClient(MONGODB_HOSTNAME, MONGODB_PORT).getDatabase(DB_NAME);
        }
        return mongoDB;
    }

    public static MongoCollection getWordCollection() {
        if (wordCollection == null) {
            wordCollection = getDatabase().getCollection(WORD_COLLECTION_NAME);
        }
        return wordCollection;
    }

    public static MongoCollection getUserRequestsCollection() {
        if (userRequestsCollection == null) {
            userRequestsCollection = getDatabase().getCollection(USER_REQUEST_COLLECTION_NAME);
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
            return Document.parse(objectMapper.writeValueAsString(object) );
        } catch (Exception ex) {
            throw new IllegalArgumentException("objectToDocument error. Object["
                + object.toString() + "][Ex:" + ex.getStackTrace().toString());
        }
    }
}
