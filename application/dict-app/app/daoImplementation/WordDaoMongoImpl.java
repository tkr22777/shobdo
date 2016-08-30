package daoImplementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import daos.WordDao;
import objects.DictionaryWord;
import org.bson.Document;
import utilities.LogPrint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordDaoMongoImpl implements WordDao {

    public final String DICTIONARY_DATABASE_NAME = "Dictionary";
    public final String WORD_COLLECTION_NAME = "Words";

    MongoClient mongoClient;
    MongoDatabase mongoDatabase;
    MongoCollection<Document> collection;

    private LogPrint log = new LogPrint(WordDaoMongoImpl.class);

    public WordDaoMongoImpl(){

        mongoClient = new MongoClient( "localhost" , 27017 );
        mongoDatabase = mongoClient.getDatabase(DICTIONARY_DATABASE_NAME);
        collection = mongoDatabase.getCollection(WORD_COLLECTION_NAME);

    }

    @Override
    public String getDictWord(String wordName) {

        Document word = collection.find(eq("Spelling",wordName)).first();
        return word.get("Meaning").toString();
    }

    @Override
    public String setDictWord(String wordName, String Meaning) {

        Document exitingWord  = collection.find( and( eq( "Spelling", wordName ) , eq("Meaning", Meaning) ) ) .first() ;

        if( exitingWord != null )
            return "BaseWord Meaning Already Exists";

        Document wordEntry = new Document("Spelling", wordName)
                .append("Meaning", Meaning);
        collection.insertOne(wordEntry);
        return wordName + "ID";

    }

    @Override
    public String setDictionaryWord(DictionaryWord dictionaryWord) {

        log.info("Saving word to database: " + dictionaryWord.toString());


        try {

            ObjectMapper mapper = new ObjectMapper();

            //String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dictionaryWord);
            //log.info("Json String for Dictionary Word: " + jsonString);

            Document wordDocument = Document.parse( mapper.writeValueAsString(dictionaryWord) );
            log.info(wordDocument.toString());
            collection.insertOne(wordDocument);

        } catch ( Exception ex ){

            log.info( "Failed to map dictionary word object to jsonString. Ex: " + ex.getMessage() );

        }

        return null;
    }

    @Override
    public DictionaryWord getDictionaryWord(String wordId, String wordSpeelling) {
        return null;
    }

    @Override
    public ArrayList<String> searchDictionaryWord(String wordSpeelling) {

        final String WORD_SPELLING = "wordSpelling";

        Pattern p = Pattern.compile("^" + wordSpeelling + ".*");

        BasicDBObject query = new BasicDBObject(WORD_SPELLING, p);

        MongoCursor<Document> words = collection.find(query).projection(Projections.include(WORD_SPELLING)).batchSize(5).iterator();

        Set<String> result = new HashSet<>();

        while(words.hasNext()) {

            Document bleh = words.tryNext();
            result.add( bleh.get(WORD_SPELLING).toString() );

        }

        return new ArrayList<>(result);
    }


}
