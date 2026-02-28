package like.stores;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import common.objects.EntityStatus;
import like.objects.Like;
import org.bson.Document;
import utilities.Constants;
import utilities.ShobdoLogger;

import java.util.ArrayList;
import java.util.List;

public class LikeStoreMongoImpl implements LikeStore {

    private final MongoCollection<Document> likesCollection;
    private static final ShobdoLogger log = new ShobdoLogger(LikeStoreMongoImpl.class);

    public LikeStoreMongoImpl(final MongoCollection<Document> likesCollection) {
        this.likesCollection = likesCollection;
    }

    @Override
    public Like getLike(final String userId, final String wordId) {
        final BasicDBObject query = Like.getActiveObjectQuery();
        query.put("userId", userId);
        query.put("wordId", wordId);
        final Document doc = likesCollection.find(query).first();
        log.debug("getLike userId:" + userId + " wordId:" + wordId + " result:" + doc);
        return doc == null ? null : Like.fromBsonDoc(doc);
    }

    @Override
    public Like createLike(final Like like) {
        likesCollection.insertOne(like.toDocument());
        log.debug("createLike id:" + like.getId());
        return like;
    }

    @Override
    public void deleteLike(final String userId, final String wordId) {
        final BasicDBObject query = Like.getActiveObjectQuery();
        query.put("userId", userId);
        query.put("wordId", wordId);
        final Document update = new Document("$set",
            new Document(Constants.MONGO_DOC_KEY_STATUS, EntityStatus.DELETED.toString()));
        likesCollection.updateOne(query, update);
        log.debug("deleteLike (soft) userId:" + userId + " wordId:" + wordId);
    }

    @Override
    public List<String> getLikedWordIds(final String userId) {
        final BasicDBObject query = Like.getActiveObjectQuery();
        query.put("userId", userId);
        final MongoCursor<Document> cursor = likesCollection.find(query)
            .projection(Projections.include("wordId"))
            .iterator();
        final List<String> wordIds = new ArrayList<>();
        while (cursor.hasNext()) {
            final Document doc = cursor.next();
            if (doc.containsKey("wordId")) {
                wordIds.add(doc.getString("wordId"));
            }
        }
        log.debug("getLikedWordIds userId:" + userId + " count:" + wordIds.size());
        return wordIds;
    }

    @Override
    public long countLikes(final String wordId) {
        final BasicDBObject query = Like.getActiveObjectQuery();
        query.put("wordId", wordId);
        return likesCollection.countDocuments(query);
    }
}
