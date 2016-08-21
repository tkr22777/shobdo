import java.awt.image.LookupOp;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import objects.DictionaryWord;
import objects.Meaning;
import objects.MeaningForPartsOfSpeech;
import objects.PartsOfSpeechSet;
import org.junit.*;

import play.Logger;
import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;
import play.twirl.api.Content;
import scala.App;
import scala.io.StdIn;
import utilities.Bangla;
import utilities.LogPrint;

import static play.test.Helpers.*;
import static org.junit.Assert.*;


/**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ApplicationTest {

    LogPrint log;

    @Test @Ignore
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test @Ignore
    public void renderTemplate() {
        //Content html = views.html.ndex.render("Your new application is ready.");
        //assertEquals("text/html", html.contentType());
        //assertTrue(html.body().contains("Your new application is ready."));
    }

    @Before
    public void setup() {

        log = new LogPrint(ApplicationTest.class);

    }

    @Test
    public void testBangla() {

        String start = "995"; //ক
        String end = "9A8";   //ন

        log.info("START!");

        PartsOfSpeechSet partsOfSpeech = new PartsOfSpeechSet();
        partsOfSpeech.setPartsOfSpeeches( new HashSet<String>(Arrays.asList( "বিশেষ্য" , "বিশেষণ", "সর্বনাম", "অব্যয়" , "ক্রিয়া" ) ) );

        log.info(partsOfSpeech.toString());

        int numberOfWords = 10;

        Set<DictionaryWord> dictionary = new HashSet<>();

        for(int i = 0 ; i < numberOfWords ; i++) {

            String wordSpelling;
            String wordId;

            int wordLength = Bangla.randomInRange(2, 9);
            wordSpelling = Bangla.getWord(start, end, wordLength);
            wordId = "WD_" + UUID.randomUUID();

            DictionaryWord dictionaryWord = new DictionaryWord(wordId, wordSpelling);

            ArrayList<MeaningForPartsOfSpeech> meaningsForPartsOfSpeech = new ArrayList<>();

            for (String pos : partsOfSpeech.getPartsOfSpeeches()) {

                MeaningForPartsOfSpeech meanings = new MeaningForPartsOfSpeech();

                int numberOfMeaningForPOS = Bangla.randomInRange(1,3);

                for(int j = 0; j < numberOfMeaningForPOS ; j++) {

                    String meaning;
                    wordLength = Bangla.randomInRange(2, 9);
                    meaning = Bangla.getWord(start, end, wordLength);

                    String example;
                    int preSentenceLen = Bangla.randomInRange(2, 6);
                    int postSentenceLen = Bangla.randomInRange(2, 4);
                    example = Bangla.getSentence(start, end, preSentenceLen, 12);
                    example += " " + meaning + " ";
                    example += Bangla.getSentence(start, end, postSentenceLen, 12);

                    String meaningId = "MN_" + UUID.randomUUID();

                    Meaning meaningForPOS = new Meaning(meaningId, pos, meaning, example);

                    meanings.setAMeaning(meaningForPOS);

                }

                meaningsForPartsOfSpeech.add(meanings);

            }

            dictionaryWord.setMeaningForPartsOfSpeeches(meaningsForPartsOfSpeech);

            dictionary.add(dictionaryWord);

        }

        int i = 0;
        for(DictionaryWord word: dictionary){
            log.info(" Word "+ i + " :" + word.toString());
            i++;
        }

    }

}
