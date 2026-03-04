package user.stores;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import user.objects.User;
import utilities.Constants;
import utilities.ShobdoLogger;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public User getUserById(final String userId) {
        final BasicDBObject query = User.getActiveObjectQueryForId(userId);
        final Document doc = usersCollection.find(query).first();
        log.debug("getUserById userId:" + userId + " result:" + doc);
        return doc == null ? null : User.fromBsonDoc(doc);
    }

    @Override
    public User updateUser(final User user) {
        final BasicDBObject query = User.getActiveObjectQueryForId(user.getId());
        usersCollection.replaceOne(query, user.toDocument());
        log.debug("updateUser id:" + user.getId());
        return user;
    }

    @Override
    public List<User> listUsers() {
        final BasicDBObject query = User.getActiveObjectQuery();
        final MongoCursor<Document> cursor = usersCollection.find(query).iterator();
        final List<User> users = new ArrayList<>();
        while (cursor.hasNext()) {
            users.add(User.fromBsonDoc(cursor.next()));
        }
        log.debug("listUsers count:" + users.size());
        return users;
    }
}
