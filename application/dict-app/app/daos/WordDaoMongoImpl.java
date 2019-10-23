package daos;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import com.typesafe.config.ConfigFactory;
import objects.Constants;
import objects.Word;
import org.bson.Document;
import utilities.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Tahsin Kabir on 8/14/16.
 */
public class WordDaoMongoImpl implements WordDao {

    private static final String COLLECTION_NAME = "Words";

    private final MongoCollection<Document> wordCollection;
    private static final ShobdoLogger log = new ShobdoLogger(WordDaoMongoImpl.class);

    public WordDaoMongoImpl() {
        final String MONGODB_HOSTNAME = ConfigFactory.load().getString("shobdo.mongodbhostname");
        final int MONGODB_PORT = Integer.parseInt(ConfigFactory.load().getString("shobdo.mongodbport"));
        log.info("@WDMI001 Connecting to mongodb [host:" + MONGODB_HOSTNAME + "][port:" + MONGODB_PORT + "]");
        wordCollection = new MongoClient(MONGODB_HOSTNAME, MONGODB_PORT)
            .getDatabase(MImplUtil.DB_NAME)
            .getCollection(COLLECTION_NAME);
    }

    @Override
    public Word create(final Word word) {
        final Document wordDoc = MImplUtil.toDocument(word);
        wordCollection.insertOne(wordDoc);
        log.info("Creating word on database: " + word.getSpelling());
        return word;
    }

    @Override
    public Word getById(final String wordId) {
        final BasicDBObject query = MImplUtil.getActiveObjectQuery();
        query.put(MImplUtil.ID_PARAM, wordId);
        final Document wordDoc = wordCollection.find(query).first();
        log.info("Retrieving word by id: " + wordId + " MongoDoc:" + wordDoc);
        return wordDoc == null ? null: MImplUtil.toWord(wordDoc);
    }

    @Override
    public Word getBySpelling(final String spelling) {
        final BasicDBObject query = MImplUtil.getActiveObjectQuery();
        query.put(Constants.SPELLING_KEY, spelling);
        final Document wordDoc = wordCollection.find(query).first();
        log.info("@WDMI004 getBySpelling spelling: " + spelling + " MongoDoc:" + wordDoc);
        return wordDoc == null ? null: MImplUtil.toWord(wordDoc);
    }

    @Override
    public Word update(final Word word) {
        final BasicDBObject query = MImplUtil.getActiveObjectQuery();
        query.put(MImplUtil.ID_PARAM, word.getId());
        final Document wordDocument = MImplUtil.toDocument(word);
        wordCollection.replaceOne(query, wordDocument);
        return word;
    }

    @Override
    public void delete(final String wordId) {
        //so delete via update/setting the deleted timestamp or flag
    }

    @Override
    public Set<String> searchSpellingsBySpelling(final String spellingQuery, final int limit) {
        final BasicDBObject query = MImplUtil.getActiveObjectQuery();
        query.put(Constants.SPELLING_KEY, Pattern.compile("^" + spellingQuery + ".*"));

        final MongoCursor<Document> words = wordCollection.find(query)
            .projection(Projections.include(Constants.SPELLING_KEY))
            .limit(limit)
            .batchSize(100)
            .iterator();

        final Set<String> result = new HashSet<>();
        while(words.hasNext()) {
            result.add(words.tryNext().get(Constants.SPELLING_KEY).toString());
        }
        log.info("@WDMI006 searching words by spelling: " + spellingQuery);
        return result;
    }

    @Override
    public long count() {
        return wordCollection.count();
    }

    public void deleteAll() {
        final DeleteResult result = wordCollection.deleteMany(new BasicDBObject());
        log.info("Delete db entries: " + result);
    }

    @Override
    public ArrayList<Word> list(final String startWordId, final int limit) {
        return new ArrayList<>();
    }

}
