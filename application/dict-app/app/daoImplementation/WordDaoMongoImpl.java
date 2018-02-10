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

    private final String WORD_ID = "id";
    private final String REQUEST_ID = "requestId";
    private final String WORD_SPELLING = "wordSpelling";

    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> collection;

    private BenchmarkLogger bmLog = new BenchmarkLogger(WordDaoMongoImpl.class);
    private LogPrint log = new LogPrint(WordDaoMongoImpl.class);

    private final String MONGODB_HOSTNAME;
    private final int MONGODB_PORT;

    public WordDaoMongoImpl() {

        MONGODB_HOSTNAME = ConfigFactory.load().getString(Constants.MONGODB_HOSTNAME_CONFIG_STRING);
        MONGODB_PORT = Integer.parseInt( ConfigFactory.load().getString(Constants.MONGODB_PORT_CONFIG_STRING));

        log.info( "@WDMI001 Connecting to mongodb [host:" + MONGODB_HOSTNAME + "][port:" + MONGODB_PORT + "]" );

        mongoClient = new MongoClient( MONGODB_HOSTNAME, MONGODB_PORT );
        mongoDatabase = mongoClient.getDatabase(Constants.DICTIONARY_DATABASE_NAME);
        collection = mongoDatabase.getCollection(Constants.WORD_COLLECTION_NAME);
    }

    @Override
    public Word createWord(Word word) {

        bmLog.start();
        Document wordDoc = JsonUtil.objectToDocument(word);
        collection.insertOne(wordDoc);
        bmLog.end("@WDMI002 createWord Saving word to database: " + word.getWordSpelling());
        return word;
    }

    @Override
    public Word getWordByWordId(String wordId) {

        bmLog.start();
        BasicDBObject query = new BasicDBObject(WORD_ID, wordId);
        log.info("Query: " + query);
        Document wordDoc = collection.find(query).first();
        bmLog.end("@WDMI003 getWordByWordId id: " + wordId + " mongoDoc:" + wordDoc);
        return wordDoc == null ?  null: DictUtil.getWordFromDocument( wordDoc, Word.class);
    }

    @Override
    public Word getWordBySpelling(String spelling) {

        bmLog.start();
        BasicDBObject query = new BasicDBObject(WORD_SPELLING, spelling);
        Document wordDoc = collection.find(query).first();
        bmLog.end("@WDMI004 getWordBySpelling   spelling: " + spelling + " mongoDoc:" + wordDoc);
        return wordDoc == null ?  null: DictUtil.getWordFromDocument( wordDoc, Word.class);
    }

    @Override
    public Word updateWord(Word word) {

        if(word.getId() == null || word.getId().trim().equals(""))
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);

        BasicDBObject searchQuery = new BasicDBObject(WORD_ID, word.getId());
        Document wordDocument = JsonUtil.objectToDocument(word);
        collection.replaceOne(searchQuery, wordDocument);
        return word;
    }

    @Override
    public void deleteWord(String wordId) {
        //so delete via update/setting the deleted timestamp or flag
    }

    @Override
    public Set<String> getWordSpellingsWithPrefixMatch(String spelling, int limit) {

        bmLog.start();

        Pattern prefixForSpellPattern = Pattern.compile("^" + spelling + ".*");
        BasicDBObject query = new BasicDBObject(WORD_SPELLING, prefixForSpellPattern);

        MongoCursor<Document> words = collection
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

        bmLog.end("@WDMI006 getWordSpellingsWithPrefixMatch getting similar spelling from database for spelling: " + spelling);
        return result;
    }

    @Override
    public long totalWordCount() {
        return collection.count();
    }

    public void deleteAllWords() {

        DeleteResult result = collection.deleteMany(new BasicDBObject());
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
        collection.insertOne(requestDoc);
        bmLog.end("@WDMI002 createRequest Saving request to database: " + request.getRequestId());
        return request;
    }

    @Override
    public UserRequest getRequestById(String requestId) {

        bmLog.start();
        BasicDBObject query = new BasicDBObject(REQUEST_ID, requestId);
        Document requestDoc = collection.find(query).first();
        bmLog.end("@WDMI003 getWordByWordId id: " + requestId + " mongoDoc:" + requestDoc);
        return requestDoc == null ?  null: DictUtil.getRequestFromDocument( requestDoc, UserRequest.class);
    }

    @Override
    public UserRequest updateRequest(UserRequest request) {

        String requestId = request.getRequestId();

        if( requestId == null || requestId.trim().length() == 0)
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);

        BasicDBObject searchQuery = new BasicDBObject(REQUEST_ID, requestId);
        Document requestDocument = JsonUtil.objectToDocument(request);
        collection.replaceOne(searchQuery, requestDocument);
        return request;
    }

    @Override
    public void deleteRequest(String requestId) {
        //so delete via update/setting the deleted timestamp, status deleted
    }

}
