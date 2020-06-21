package request;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import common.stores.MongoQuery;
import request.objects.UserRequest;
import org.bson.Document;
import utilities.ShobdoLogger;
import word.WordStoreMongoImpl;

import java.util.ArrayList;

public class UserRequestStoreMongoImpl implements UserRequestStore {

    private static final ShobdoLogger log = new ShobdoLogger(WordStoreMongoImpl.class);
    private final MongoCollection<Document> userRequestCollection;

    public UserRequestStoreMongoImpl(MongoCollection userRequestCollection) {
        this.userRequestCollection = userRequestCollection;
    }

    @Override
    public UserRequest create(final UserRequest request) {
        final Document requestDoc = request.document();
        userRequestCollection.insertOne(requestDoc);
        log.info("@WDMI002 createUserRequest Saving request to database: " + request.getId());
        return request;
    }

    @Override
    public UserRequest get(String id) {
        final BasicDBObject query = MongoQuery.getActiveObjectQueryForId(id);
        final Document requestDoc = userRequestCollection.find(query).first();
        log.info("@WDMI003 getById id: " + id + " mongoDoc:" + requestDoc);
        return requestDoc == null ?  null: UserRequest.fromBsonDoc(requestDoc);
    }

    @Override
    public UserRequest update(UserRequest request) {
        final BasicDBObject query = MongoQuery.getActiveObjectQueryForId(request.getId());
        final Document requestDoc = request.document();
        userRequestCollection.replaceOne(query, requestDoc);
        return request;
    }

    @Override
    public void delete(String requestId) {
        //TODO implement
        //so delete via update/setting the deleted timestamp, status deleted
    }

    @Override
    public long count() {
        //TODO implement
        return 0;
    }

    @Override
    public ArrayList<UserRequest> list(String startId, int limit) {
        //TODO implement
        return null;
    }
}
