package daoImplementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import daos.WordDao;
import objects.DictionaryWord;
import org.bson.Document;
import utilities.BenchmarkLogger;
import utilities.DictUtil;
import utilities.LogPrint;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordDaoMongoImpl implements WordDao {

    public final String DICTIONARY_DATABASE_NAME = "Dictionary";
    public final String WORD_COLLECTION_NAME = "Words";

    private final String WORD_ID = "wordId";
    private final String WORD_SPELLING = "wordSpelling";

    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> collection;

    private BenchmarkLogger bmLog = new BenchmarkLogger(WordDaoMongoImpl.class);
    private LogPrint log = new LogPrint(WordDaoMongoImpl.class);

    public WordDaoMongoImpl() {

        String MONGODB_HOSTNAME = "mongo";
        int MONGODB_PORT = 27017;

        //MONGODB_HOSTNAME = "localhost";
        //MONGODB_HOSTNAME = "172.17.0.1";
        log.info( "@WDMI001 Connecting to mongodb [host:" + MONGODB_HOSTNAME + "][port:" + MONGODB_PORT + "]" );

        mongoClient = new MongoClient( MONGODB_HOSTNAME, MONGODB_PORT );
        mongoDatabase = mongoClient.getDatabase(DICTIONARY_DATABASE_NAME);
        collection = mongoDatabase.getCollection(WORD_COLLECTION_NAME);
    }

    @Override
    public String setDictionaryWord(DictionaryWord dictionaryWord) {

        bmLog.start();

        try {

            ObjectMapper mapper = new ObjectMapper();
            Document wordDocument = Document.parse( mapper.writeValueAsString(dictionaryWord) );
            collection.insertOne(wordDocument);
            bmLog.end("@WDMI001 setDictionaryWord Saving word to database: " + dictionaryWord.getWordSpelling());

        } catch ( Exception ex ){

            log.info( "Failed to map dictionary word object to jsonString. Ex: " + ex.getMessage() );
        }

        return null;
    }

    @Override
    public DictionaryWord getDictionaryWordByWordId(String wordId) {

        bmLog.start();

        BasicDBObject query = new BasicDBObject(WORD_ID, wordId);
        Document wordDocument = collection.find(query).first();

        if(wordDocument == null) {

            bmLog.end("@WDMI002 getDictionaryWordByWordId word not found in database for wordId: " + wordId);
            return null;

        } else  {

            DictionaryWord word = (DictionaryWord) DictUtil.getDictionaryWordFromDocument( wordDocument, DictionaryWord.class);
            bmLog.end("@WDMI002 getDictionaryWordByWordId word [spelling:" + word.getWordSpelling()
                    + "] found from database for wordId: " + wordId);
            return word;
        }
    }

    @Override
    public DictionaryWord getDictionaryWordBySpelling(String spelling) {

        bmLog.start();

        BasicDBObject query = new BasicDBObject(WORD_SPELLING, spelling);
        Document wordDocument = collection.find(query).first();

        if(wordDocument == null) {

            bmLog.end("@WDMI004 getDictionaryWordByWordId word not found in database for spelling: " + spelling);
            return null;

        } else {

            bmLog.end("@WDMI005 getDictionaryWordByWordId word found in database for spelling: " + spelling);
            DictionaryWord word = (DictionaryWord) DictUtil.getDictionaryWordFromDocument( wordDocument, DictionaryWord.class);
            return word;
        }
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
}
