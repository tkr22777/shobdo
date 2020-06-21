package word;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import common.store.MongoStoreFactory;
import utilities.Constants;
import org.bson.Document;
import utilities.*;
import word.objects.Word;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class WordStoreMongoImpl implements WordStore {

    private final MongoCollection<Document> wordCollection;
    private static final ShobdoLogger log = new ShobdoLogger(WordStoreMongoImpl.class);

    public WordStoreMongoImpl(MongoCollection wordCollection) {
        this.wordCollection = wordCollection;
    }

    @Override
    public Word create(final Word word) {
        final Document wordDoc = MongoStoreFactory.toDocument(word);
        wordCollection.insertOne(wordDoc);
        log.debug("Creating word on database: " + word.getSpelling());
        return word;
    }

    @Override
    public Word getById(final String wordId) {
        final BasicDBObject query = MongoStoreFactory.getActiveObjectQuery();
        query.put(MongoStoreFactory.ID_PARAM, wordId);
        final Document wordDoc = wordCollection.find(query).first();
        log.debug("Retrieving word by id: " + wordId + " MongoDoc:" + wordDoc);
        return wordDoc == null ? null: MongoStoreFactory.toWord(wordDoc);
    }

    @Override
    public Word getBySpelling(final String spelling) {
        final BasicDBObject query = MongoStoreFactory.getActiveObjectQuery();
        query.put(Constants.KEY_SPELLING, spelling);
        final Document wordDoc = wordCollection.find(query).first();
        log.debug("@WDMI004 getBySpelling spelling: " + spelling + " MongoDoc:" + wordDoc);
        return wordDoc == null ? null: MongoStoreFactory.toWord(wordDoc);
    }

    @Override
    public Word update(final Word word) {
        final BasicDBObject query = MongoStoreFactory.getActiveObjectQuery();
        query.put(MongoStoreFactory.ID_PARAM, word.getId());
        final Document wordDocument = MongoStoreFactory.toDocument(word);
        wordCollection.replaceOne(query, wordDocument);
        return word;
    }

    @Override
    public void delete(final String wordId) {
        //TODO implement/verify
        //so delete via update/setting the deleted timestamp or flag
    }

    @Override
    public Set<String> searchSpellingsBySpelling(final String spellingQuery, final int limit) {
        final BasicDBObject query = MongoStoreFactory.getActiveObjectQuery();
        query.put(Constants.KEY_SPELLING, Pattern.compile("^" + spellingQuery + ".*"));

        final MongoCursor<Document> words = wordCollection.find(query)
            .projection(Projections.include(Constants.KEY_SPELLING))
            .limit(limit)
            .batchSize(100)
            .iterator();

        final Set<String> result = new HashSet<>();
        while (words.hasNext()) {
            result.add(words.tryNext().get(Constants.KEY_SPELLING).toString());
        }
        log.debug("@WDMI006 searching words by spelling: " + spellingQuery);
        return result;
    }

    @Override
    public long count() {
        //TODO implement/verify
        return wordCollection.count();
    }

    public void deleteAll() {
        //TODO implement/verify
        final DeleteResult result = wordCollection.deleteMany(new BasicDBObject());
        log.debug("Delete db entries: " + result);
    }

    @Override
    public ArrayList<Word> list(final String startWordId, final int limit) {
        //TODO implement/verify
        return new ArrayList<>();
    }

}
