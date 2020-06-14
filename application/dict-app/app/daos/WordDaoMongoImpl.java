package daos;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import objects.Constants;
import objects.Word;
import org.bson.Document;
import utilities.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class WordDaoMongoImpl implements WordDao {

    private final MongoCollection<Document> wordCollection;
    private static final ShobdoLogger log = new ShobdoLogger(WordDaoMongoImpl.class);

    public WordDaoMongoImpl() {
        wordCollection = MongoManager.getWordCollection();
    }

    @Override
    public Word create(final Word word) {
        final Document wordDoc = MongoManager.toDocument(word);
        wordCollection.insertOne(wordDoc);
        log.debug("Creating word on database: " + word.getSpelling());
        return word;
    }

    @Override
    public Word getById(final String wordId) {
        final BasicDBObject query = MongoManager.getActiveObjectQuery();
        query.put(MongoManager.ID_PARAM, wordId);
        final Document wordDoc = wordCollection.find(query).first();
        log.debug("Retrieving word by id: " + wordId + " MongoDoc:" + wordDoc);
        return wordDoc == null ? null: MongoManager.toWord(wordDoc);
    }

    @Override
    public Word getBySpelling(final String spelling) {
        final BasicDBObject query = MongoManager.getActiveObjectQuery();
        query.put(Constants.SPELLING_KEY, spelling);
        final Document wordDoc = wordCollection.find(query).first();
        log.debug("@WDMI004 getBySpelling spelling: " + spelling + " MongoDoc:" + wordDoc);
        return wordDoc == null ? null: MongoManager.toWord(wordDoc);
    }

    @Override
    public Word update(final Word word) {
        final BasicDBObject query = MongoManager.getActiveObjectQuery();
        query.put(MongoManager.ID_PARAM, word.getId());
        final Document wordDocument = MongoManager.toDocument(word);
        wordCollection.replaceOne(query, wordDocument);
        return word;
    }

    @Override
    public void delete(final String wordId) {
        //so delete via update/setting the deleted timestamp or flag
    }

    @Override
    public Set<String> searchSpellingsBySpelling(final String spellingQuery, final int limit) {
        final BasicDBObject query = MongoManager.getActiveObjectQuery();
        query.put(Constants.SPELLING_KEY, Pattern.compile("^" + spellingQuery + ".*"));

        final MongoCursor<Document> words = wordCollection.find(query)
            .projection(Projections.include(Constants.SPELLING_KEY))
            .limit(limit)
            .batchSize(100)
            .iterator();

        final Set<String> result = new HashSet<>();
        while (words.hasNext()) {
            result.add(words.tryNext().get(Constants.SPELLING_KEY).toString());
        }
        log.debug("@WDMI006 searching words by spelling: " + spellingQuery);
        return result;
    }

    @Override
    public long count() {
        return wordCollection.count();
    }

    public void deleteAll() {
        final DeleteResult result = wordCollection.deleteMany(new BasicDBObject());
        log.debug("Delete db entries: " + result);
    }

    @Override
    public ArrayList<Word> list(final String startWordId, final int limit) {
        return new ArrayList<>();
    }

}
