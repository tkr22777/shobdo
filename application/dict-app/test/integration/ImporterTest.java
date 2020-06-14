package integration;

import importer.SamsadImporter;
import logics.WordLogic;
import objects.Word;
import org.junit.Test;
import play.test.WithServer;
import utilities.ShobdoLogger;

import java.util.List;

/**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ImporterTest extends WithServer {

    ShobdoLogger log = new ShobdoLogger(ImporterTest.class);
    WordLogic wordLogic = WordLogic.createMongoBackedWordLogic();

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
