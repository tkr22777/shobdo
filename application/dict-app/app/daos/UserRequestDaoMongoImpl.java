package daos;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import objects.UserRequest;
import org.bson.Document;
import utilities.ShobdoLogger;

import java.util.ArrayList;

public class UserRequestDaoMongoImpl implements UserRequestDao {

    private static final ShobdoLogger log = new ShobdoLogger(WordDaoMongoImpl.class);
    private final MongoCollection<Document> userRequestCollection;

    public UserRequestDaoMongoImpl() {
        userRequestCollection = MongoManager.getUserRequestsCollection();
    }

    @Override
    public UserRequest create(final UserRequest request) {
        final Document requestDoc = MongoManager.toDocument(request);
        userRequestCollection.insertOne(requestDoc);
        log.info("@WDMI002 createUserRequest Saving request to database: " + request.getId());
        return request;
    }

    @Override
    public UserRequest get(String id) {
        final BasicDBObject query = MongoManager.getActiveObjectQuery();
        query.put(MongoManager.ID_PARAM, id);

        final Document requestDoc = userRequestCollection.find(query).first();
        log.info("@WDMI003 getById id: " + id + " mongoDoc:" + requestDoc);
        return requestDoc == null ?  null: MongoManager.toUserRequest(requestDoc);
    }

    @Override
    public UserRequest update(UserRequest request) {
        final BasicDBObject query = MongoManager.getActiveObjectQuery();
        query.put(MongoManager.ID_PARAM, request.getId());

        final Document requestDoc = MongoManager.toDocument(request);
        userRequestCollection.replaceOne(query, requestDoc);
        return request;
    }

    @Override
    public void delete(String requestId) {
        //so delete via update/setting the deleted timestamp, status deleted
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public ArrayList<UserRequest> list(String startId, int limit) {
        return null;
    }
}
