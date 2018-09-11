package daoImplementation;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import com.typesafe.config.ConfigFactory;
import daos.WordDao;
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
 * Created by tahsinkabir on 8/14/16.
 */
public class WordDaoMongoImpl implements WordDao {

    private static final String MONGODB_HOSTNAME_CONFIG_STRING = "shobdo.mongodbhostname";
    private static final String MONGODB_PORT_CONFIG_STRING = "shobdo.mongodbport";
    private static final String DB_NAME = "Dictionary";
    private static final String COLLECTION_NAME = "Words";

    private static final String WORD_ID = "id";
    private static final String REQUEST_ID = "requestId";
    private static final String WORD_SPELLING = "wordSpelling";
    private static final String ENTITYMETA_STATUS = "entityMeta.status";

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final MongoCollection<Document> mongoCollection;

    private static final BenchmarkLogger bmLog = new BenchmarkLogger(WordDaoMongoImpl.class);
    private static final LogPrint log = new LogPrint(WordDaoMongoImpl.class);

    private final String MONGODB_HOSTNAME;
    private final int MONGODB_PORT;

    public WordDaoMongoImpl() {

        MONGODB_HOSTNAME = ConfigFactory.load().getString(MONGODB_HOSTNAME_CONFIG_STRING);
        MONGODB_PORT = Integer.parseInt(ConfigFactory.load().getString(MONGODB_PORT_CONFIG_STRING));

        log.info( "@WDMI001 Connecting to mongodb [host:" + MONGODB_HOSTNAME + "][port:" + MONGODB_PORT + "]");

        mongoClient = new MongoClient(MONGODB_HOSTNAME, MONGODB_PORT);
        mongoDatabase = mongoClient.getDatabase(DB_NAME);
        mongoCollection = mongoDatabase.getCollection(COLLECTION_NAME);
    }

    @Override
    public Word createWord(Word word) {
        bmLog.start();
        Document wordDoc = JsonUtil.objectToDocument(word);
        mongoCollection.insertOne(wordDoc);
        bmLog.end("@WDMI002 createWord Saving word to database: " + word.getWordSpelling());
        return word;
    }

    @Override
    public Word getWordByWordId(String wordId) {
        bmLog.start();
        BasicDBObject query = new BasicDBObject();
        query.put(WORD_ID, wordId);
        query.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());

        log.info("Query: " + query);
        Document wordDoc = mongoCollection.find(query).first();
        bmLog.end("@WDMI003 getWordByWordId id: " + wordId + " mongoDoc:" + wordDoc);
        return wordDoc == null ?  null: DictUtil.getWordFromDocument( wordDoc, Word.class);
    }

    @Override
    public Word getWordBySpelling(String spelling) {
        bmLog.start();
        BasicDBObject query = new BasicDBObject();
        query.put(WORD_SPELLING, spelling);
        query.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());

        Document wordDoc = mongoCollection.find(query).first();
        bmLog.end("@WDMI004 getWordBySpelling spelling: " + spelling + " mongoDoc:" + wordDoc);
        return wordDoc == null ?  null: DictUtil.getWordFromDocument( wordDoc, Word.class);
    }

    @Override
    public Word updateWord(Word word) {
        if(word.getId() == null || word.getId().trim().equals(""))
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);

        BasicDBObject query = new BasicDBObject();
        query.put(WORD_ID, word.getId());
        query.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());

        Document wordDocument = JsonUtil.objectToDocument(word);
        mongoCollection.replaceOne(query, wordDocument);
        return word;
    }

    @Override
    public void deleteWord(String wordId) {
        //so delete via update/setting the deleted timestamp or flag
    }

    @Override
    public Set<String> searchWordSpellingsWithPrefixMatch(String spelling, int limit) {

        bmLog.start();

        Pattern prefixForSpellPattern = Pattern.compile("^" + spelling + ".*");
        BasicDBObject query = new BasicDBObject();
        query.put(WORD_SPELLING, prefixForSpellPattern);
        query.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());

        MongoCursor<Document> words = mongoCollection
                .find(query)
                .projection(Projections.include(WORD_SPELLING))
                .limit(limit)
                .batchSize(100)
                .iterator();

        Set<String> result = new HashSet<>();

        while(words.hasNext()) {

            Document document = words.tryNext();
            result.add( document.get(WORD_SPELLING).toString() );
        }

        bmLog.end("@WDMI006 searchWordSpellingsWithPrefixMatch getting similar spelling from database for spelling: " + spelling);
        return result;
    }

    @Override
    public long totalWordCount() {
        return mongoCollection.count();
    }

    public void deleteAllWords() {

        DeleteResult result = mongoCollection.deleteMany(new BasicDBObject());
        log.info("Result : " + result);
    }

    @Override
    public ArrayList<Word> listWords(String startWordId, int limit) {
        return new ArrayList<>();
    }

    /* Consider moving them to request */
    @Override
    public UserRequest createRequest(UserRequest request) {
        bmLog.start();
        Document requestDoc = JsonUtil.objectToDocument(request);
        mongoCollection.insertOne(requestDoc);
        bmLog.end("@WDMI002 createRequest Saving request to database: " + request.getRequestId());
        return request;
    }

    @Override
    public UserRequest getRequestById(String requestId) {

        bmLog.start();

        BasicDBObject query = new BasicDBObject();
        query.put(REQUEST_ID, requestId);
        query.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());

        Document requestDoc = mongoCollection.find(query).first();
        bmLog.end("@WDMI003 getWordByWordId id: " + requestId + " mongoDoc:" + requestDoc);
        return requestDoc == null ?  null: DictUtil.getRequestFromDocument( requestDoc, UserRequest.class);
    }

    @Override
    public UserRequest updateRequest(UserRequest request) {

        String requestId = request.getRequestId();

        if( requestId == null || requestId.trim().length() == 0)
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put(REQUEST_ID, requestId);
        searchQuery.put(ENTITYMETA_STATUS, EntityStatus.ACTIVE.toString());

        Document requestDoc = JsonUtil.objectToDocument(request);
        mongoCollection.replaceOne(searchQuery, requestDoc);
        return request;
    }

    @Override
    public void deleteRequest(String requestId) {
        //so delete via update/setting the deleted timestamp, status deleted
    }

}
