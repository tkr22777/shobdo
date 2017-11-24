package daoImplementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import com.typesafe.config.ConfigFactory;
import daos.WordDao;
import objects.Meaning;
import objects.Word;
import org.bson.Document;
import utilities.BenchmarkLogger;
import utilities.Constants;
import utilities.DictUtil;
import utilities.LogPrint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordDaoMongoImpl implements WordDao {

    private final String WORD_ID = "wordId";
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
    public boolean createWord(Word word) {

        bmLog.start();

        try {

            ObjectMapper mapper = new ObjectMapper();
            Document wordDocument = Document.parse( mapper.writeValueAsString(word) );
            collection.insertOne(wordDocument);
            bmLog.end("@WDMI001 createWord Saving word to database: " + word.getWordSpelling());

        } catch ( Exception ex ){

            log.info( "Failed to map dictionary word object to jsonString. Ex: " + ex.getMessage() );
        }

        return true;
    }

    @Override
    public Word getWordByWordId(String wordId) {

        bmLog.start();

        BasicDBObject query = new BasicDBObject(WORD_ID, wordId);
        Document wordDocument = collection.find(query).first();

        if(wordDocument == null) {

            bmLog.end("@WDMI002 getWordByWordId word not found in database for wordId: " + wordId);
            return null;
        }

        Word word = (Word) DictUtil.getWordFromDocument( wordDocument, Word.class);
        bmLog.end("@WDMI002 getWordByWordId word [spelling:" + word.getWordSpelling()
                + "] found from database for wordId: " + wordId);
        return word;
    }

    @Override
    public Word getWordBySpelling(String spelling) {

        bmLog.start();

        BasicDBObject query = new BasicDBObject(WORD_SPELLING, spelling);
        Document wordDocument = collection.find(query).first();

        if(wordDocument == null) {

            bmLog.end("@WDMI004 getWordByWordId word not found in database for spelling: " + spelling);
            return null;
        }

        bmLog.end("@WDMI005 getWordByWordId word found in database for spelling: " + spelling);
        Word word = (Word) DictUtil.getWordFromDocument( wordDocument, Word.class);
        return word;
    }

    @Override
    public boolean updateWord(Word word) {

        return false;
    }

    @Override
    public boolean deleteWord(String wordId) {

        return false;
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

}
