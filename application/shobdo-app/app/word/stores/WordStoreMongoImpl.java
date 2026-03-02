package word.stores;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import java.util.Arrays;
import utilities.Constants;
import utilities.ShobdoLogger;
import word.objects.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class WordStoreMongoImpl implements WordStore {

    private final MongoCollection<Document> wordCollection;
    private static final ShobdoLogger log = new ShobdoLogger(WordStoreMongoImpl.class);

    public WordStoreMongoImpl(MongoCollection<Document> wordCollection) {
        this.wordCollection = wordCollection;
    }

    @Override
    public Word create(final Word word) {
        final Document wordDoc = word.toDocument();
        wordCollection.insertOne(wordDoc);
        log.debug("Creating word on database: " + word.getSpelling());
        return word;
    }

    @Override
    public Word getById(final String wordId) {
        final BasicDBObject query = Word.getActiveObjectQueryForId(wordId);
        final Document wordDoc = wordCollection.find(query).first();
        log.debug("Retrieving word by id: " + wordId + " MongoDoc:" + wordDoc);
        return wordDoc == null ? null: Word.fromBsonDoc(wordDoc);
    }

    @Override
    public Word getBySpelling(final String spelling) {
        final BasicDBObject query = Word.getActiveObjectQuery();
        query.put(Constants.KEY_SPELLING, spelling);
        final Document wordDoc = wordCollection.find(query).first();
        log.debug("@WDMI004 getBySpelling spelling: " + spelling + " MongoDoc:" + wordDoc);
        return wordDoc == null ? null: Word.fromBsonDoc(wordDoc);
    }

    @Override
    public Word update(final Word word) {
        final BasicDBObject query = Word.getActiveObjectQueryForId(word.getId());
        final Document wordDocument = word.toDocument();
        wordCollection.replaceOne(query, wordDocument);
        return word;
    }

    @Override
    public void delete(final String wordId) {
        //Actually implemented via update, check @WordLogic
    }

    @Override
    public List<Word> searchWords(final String spellingQuery, final int limit) {
        final BasicDBObject query = Word.getActiveObjectQuery();
        query.put(Constants.KEY_SPELLING, Pattern.compile("^" + spellingQuery + ".*"));

        final MongoCursor<Document> cursor = wordCollection.find(query)
            .projection(Projections.include(Constants.MONGO_DOC_KEY_ID, Constants.KEY_SPELLING))
            .limit(limit)
            .batchSize(100)
            .iterator();

        final List<Word> result = new ArrayList<>();
        while (cursor.hasNext()) {
            final Document doc = cursor.next();
            result.add(Word.builder()
                .id(doc.getString(Constants.MONGO_DOC_KEY_ID))
                .spelling(doc.getString(Constants.KEY_SPELLING))
                .build());
        }
        log.debug("@WDMI006 searching words by spelling: " + spellingQuery + " results: " + result.size());
        return result;
    }

    @Override
    public Word getWordAtIndex(final int index) {
        final BasicDBObject query = Word.getActiveObjectQuery();
        final Document wordDoc = wordCollection.find(query).skip(index).limit(1).first();
        log.debug("getWordAtIndex index: " + index + " result: " + wordDoc);
        return wordDoc == null ? null : Word.fromBsonDoc(wordDoc);
    }

    @Override
    public Word getRandomWord() {
        final Document matchStage = new Document("$match", Word.getActiveObjectQuery());
        final Document sampleStage = new Document("$sample", new Document("size", 1));
        final Document wordDoc = wordCollection.aggregate(Arrays.asList(matchStage, sampleStage)).first();
        log.debug("getRandomWord result: " + wordDoc);
        return wordDoc == null ? null : Word.fromBsonDoc(wordDoc);
    }

    @Override
    public long count() {
        //TODO implement/verify
        return wordCollection.countDocuments();
    }

    public void deleteAll() {
        //TODO remove allowing mass deletion from here. Move to test section
        final DeleteResult result = wordCollection.deleteMany(new BasicDBObject());
        log.debug("Delete db entries: " + result);
    }

    @Override
    public ArrayList<Word> list(final String startWordId, final int limit) {
        //TODO implement/verify
        return new ArrayList<>();
    }
}
