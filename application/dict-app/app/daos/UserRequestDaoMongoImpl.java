package daos;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.typesafe.config.ConfigFactory;
import objects.EntityStatus;
import objects.UserRequest;
import org.bson.Document;
import utilities.JsonUtil;
import utilities.ShobdoLogger;

import java.util.ArrayList;

public class UserRequestDaoMongoImpl implements UserRequestDao {

    private static final String DB_NAME = "Dictionary";
    private static final String COLLECTION_NAME = "Words";

    private static final ShobdoLogger log = new ShobdoLogger(WordDaoMongoImpl.class);
    private final MongoCollection<Document> userRequestCollection;

    public UserRequestDaoMongoImpl() {
        final String MONGODB_HOSTNAME = ConfigFactory.load().getString("shobdo.mongodbhostname");
        final int MONGODB_PORT = Integer.parseInt(ConfigFactory.load().getString("shobdo.mongodbport"));
        log.info("@RDMI001 Connecting to mongodb [host:" + MONGODB_HOSTNAME + "][port:" + MONGODB_PORT + "]");
        userRequestCollection = new MongoClient(MONGODB_HOSTNAME, MONGODB_PORT)
            .getDatabase(DB_NAME)
            .getCollection(COLLECTION_NAME);
    }

    @Override
    public UserRequest create(final UserRequest request) {
        final Document requestDoc = JsonUtil.objectToDocument(request);
        userRequestCollection.insertOne(requestDoc);
        log.info("@WDMI002 createUserRequest Saving request to database: " + request.getId());
        return request;
    }

    @Override
    public UserRequest get(String id) {
        final BasicDBObject query = MongoImplUtil.getActiveObjectQuery();
        query.put(MongoImplUtil.ID_PARAM, id);

        final Document requestDoc = userRequestCollection.find(query).first();
        log.info("@WDMI003 getById id: " + id + " mongoDoc:" + requestDoc);
        return requestDoc == null ?  null: MongoImplUtil.getUserRequestFromDocument(requestDoc, UserRequest.class);
    }

    @Override
    public UserRequest update(UserRequest request) {
        final BasicDBObject query = MongoImplUtil.getActiveObjectQuery();
        query.put(MongoImplUtil.ID_PARAM, request.getId());

        final Document requestDoc = JsonUtil.objectToDocument(request);
        userRequestCollection.replaceOne(query, requestDoc);
        return request;
    }

    @Override
    public void delete(String requestId) {
        //so delete via update/setting the deleted timestamp, status deleted
    }

    @Override
    public long totalCount() {
        return 0;
    }

    @Override
    public ArrayList<UserRequest> list(String startId, int limit) {
        return null;
    }
}
