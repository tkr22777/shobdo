package daoImplementation;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import daos.WordDao;
import objects.DictionaryWord;
import org.bson.Document;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordDaoImpl implements WordDao {

    public final String DICTIONARY_DATABASE_NAME = "Dictionary";
    public final String WORD_COLLECTION_NAME = "Words";

    MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
    MongoDatabase mongoDatabase = mongoClient.getDatabase(DICTIONARY_DATABASE_NAME);
    MongoCollection<Document> collection = mongoDatabase.getCollection(WORD_COLLECTION_NAME);

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
        return null;
    }

    @Override
    public DictionaryWord getDictionaryWord(String wordId, String wordSpeelling) {
        return null;
    }
}
