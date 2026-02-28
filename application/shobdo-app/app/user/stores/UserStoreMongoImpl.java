package user.stores;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import user.objects.User;
import utilities.Constants;
import utilities.ShobdoLogger;

public class UserStoreMongoImpl implements UserStore {

    private final MongoCollection<Document> usersCollection;
    private static final ShobdoLogger log = new ShobdoLogger(UserStoreMongoImpl.class);

    public UserStoreMongoImpl(final MongoCollection<Document> usersCollection) {
        this.usersCollection = usersCollection;
    }

    @Override
    public User getUserByGoogleId(final String googleId) {
        final BasicDBObject query = User.getActiveObjectQuery();
        query.put("googleId", googleId);
        final Document doc = usersCollection.find(query).first();
        log.debug("getUserByGoogleId googleId:" + googleId + " result:" + doc);
        return doc == null ? null : User.fromBsonDoc(doc);
    }

    @Override
    public User createUser(final User user) {
        usersCollection.insertOne(user.toDocument());
        log.debug("createUser id:" + user.getId());
        return user;
    }
}
