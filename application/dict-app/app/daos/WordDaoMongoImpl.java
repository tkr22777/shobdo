package daos;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import com.typesafe.config.ConfigFactory;
import exceptions.EntityDoesNotExist;
import objects.Constants;
import objects.EntityStatus;
import objects.UserRequest;
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

    private static final String DB_NAME = "Dictionary";
    private static final String COLLECTION_NAME = "Words";

    private static final String WORD_ID = "id";
    private static final String REQUEST_ID = "requestId";
    private static final String WORD_SPELLING = "wordSpelling";
    private static final String ENTITYMETA_STATUS = "entityMeta.status";

    private final MongoCollection<Document> wordCollection;
    private static final LogPrint log = new LogPrint(WordDaoMongoImpl.class);

    public WordDaoMongoImpl() {
        final String MONGODB_HOSTNAME = ConfigFactory.load().getString("shobdo.mongodbhostname");
        final int MONGODB_PORT = Integer.parseInt(ConfigFactory.load().getString("shobdo.mongodbport"));
        log.info("@WDMI001 Connecting to mongodb [host:" + MONGODB_HOSTNAME + "][port:" + MONGODB_PORT + "]");
        wordCollection = new MongoClient(MONGODB_HOSTNAME, MONGODB_PORT)
            .getDatabase(DB_NAME)
            .getCollection(COLLECTION_NAME);
    }

    @Override
    public Word create(final Word word) {
        final Document wordDoc = JsonUtil.objectToDocument(word);
        wordCollection.insertOne(wordDoc);
        log.info("Creating word on database: " + word.getWordSpelling());
        return word;
    }

    @Override
    public Word getById(final String wordId) {
        final BasicDBObject query = new BasicDBObject();
        query.put(WORD_ID, wordId);
        query.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());
        final Document wordDoc = wordCollection.find(query).first();
        log.info("Retrieving word by id: " + wordId + " MongoDoc:" + wordDoc);
        return wordDoc == null ? null: DictUtil.getWordFromDocument(wordDoc, Word.class);
    }

    @Override
    public Word getBySpelling(final String spelling) {
        final BasicDBObject query = new BasicDBObject();
        query.put(WORD_SPELLING, spelling);
        query.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());
        final Document wordDoc = wordCollection.find(query).first();
        log.info("@WDMI004 getBySpelling spelling: " + spelling + " MongoDoc:" + wordDoc);
        return wordDoc == null ? null: DictUtil.getWordFromDocument(wordDoc, Word.class);
    }

    @Override
    public Word update(final Word word) {
        final BasicDBObject query = new BasicDBObject();
        query.put(WORD_ID, word.getId());
        query.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());
        final Document wordDocument = JsonUtil.objectToDocument(word);
        wordCollection.replaceOne(query, wordDocument);
        return word;
    }

    @Override
    public void delete(final String wordId) {
        //so delete via update/setting the deleted timestamp or flag
    }

    @Override
    public Set<String> searchSpellingsBySpelling(final String spellingQuery, final int limit) {
        final BasicDBObject query = new BasicDBObject();
        query.put(WORD_SPELLING, Pattern.compile("^" + spellingQuery + ".*"));
        query.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());

        final MongoCursor<Document> words = wordCollection.find(query)
            .projection(Projections.include(WORD_SPELLING))
            .limit(limit)
            .batchSize(100)
            .iterator();

        final Set<String> result = new HashSet<>();
        while(words.hasNext()) {
            result.add(words.tryNext().get(WORD_SPELLING).toString());
        }
        log.info("@WDMI006 searching words by spelling: " + spellingQuery);
        return result;
    }

    @Override
    public long totalCount() {
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

    /* Consider moving them to request dao with requests collection */
    @Override
    public UserRequest createRequest(final UserRequest request) {
        final Document requestDoc = JsonUtil.objectToDocument(request);
        wordCollection.insertOne(requestDoc);
        log.info("@WDMI002 createRequest Saving request to database: " + request.getRequestId());
        return request;
    }

    @Override
    public UserRequest getRequest(final String requestId) {
        final BasicDBObject query = new BasicDBObject();
        query.put(REQUEST_ID, requestId);
        query.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());

        final Document requestDoc = wordCollection.find(query).first();
        log.info("@WDMI003 getById id: " + requestId + " mongoDoc:" + requestDoc);
        return requestDoc == null ?  null: DictUtil.getRequestFromDocument(requestDoc, UserRequest.class);
    }

    @Override
    public UserRequest updateRequest(final UserRequest request) {
        final BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(REQUEST_ID, request.getRequestId());
        searchQuery.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());

        final Document requestDoc = JsonUtil.objectToDocument(request);
        wordCollection.replaceOne(searchQuery, requestDoc);
        return request;
    }

    @Override
    public void deleteRequest(final String requestId) {
        //so delete via update/setting the deleted timestamp, status deleted
    }
}
