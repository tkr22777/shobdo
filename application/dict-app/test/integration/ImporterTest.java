package integration;

import common.stores.MongoStoreFactory;
import importer.SamsadImporter;
import org.junit.Test;
import play.test.WithServer;
import utilities.ShobdoLogger;
import word.WordCache;
import word.WordLogic;
import word.WordStoreMongoImpl;
import word.objects.Word;

import java.util.List;

public class ImporterTest extends WithServer {

    ShobdoLogger log = new ShobdoLogger(ImporterTest.class);
    WordStoreMongoImpl storeMongo = new WordStoreMongoImpl(MongoStoreFactory.getWordCollection());
    WordLogic wordLogic = new WordLogic(storeMongo, WordCache.getCache());

    @Test
    public void testImporter() throws Exception {
        List<Word> words = new SamsadImporter().getDictiionary();
        /*
        words.forEach(word ->  {
            try {
                wordLogic.createWord(word);
            } catch (Exception ex) {
            }
        });
        */
    }

    /*
    @Test @Ignore
    public void createDictionaryFromSamsad() throws Exception {

        Collection<Word> words = new SamsadExporter().getDictiionary();

        int total = 0;
        for(Word word: words) {

            if(total == 0) break;

            if( "YES".equalsIgnoreCase(word.retrieveExtraMetaValuesForKey("SIMPLE_SPELLING"))
             && "YES".equalsIgnoreCase(word.retrieveExtraMetaValuesForKey("SIMPLE_MEANING"))
             && "YES".equalsIgnoreCase(word.retrieveExtraMetaValuesForKey("UNDERSTANDABLE_TYPE")) ) {

                log.info("Next word: \n" + word.toString());

                total++;
            }
            wordLogic.createWord(word);
            total++;
        }

        log.info("Total words: " + total);
    }
    */
}
