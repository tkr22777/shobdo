import java.util.*;

import logics.WordLogic;
import objects.DictionaryWord;
import objects.Meaning;
import objects.MeaningForPartsOfSpeech;
import objects.PartsOfSpeechSet;
import org.junit.*;

import utilities.Bangla;
import utilities.LogPrint;
import utilities.DictUtil;

import static org.junit.Assert.*;


/**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ApplicationTest {

    LogPrint log;

    String start = "995"; //ক
    String end = "9A8";   //ন

    int numberOfWords = 10;

    PartsOfSpeechSet partsOfSpeech = new PartsOfSpeechSet();
    Set<DictionaryWord> dictionary = new HashSet<>();

    WordLogic wordLogic;

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

        dictionary = generateDictionaryWithRandomWords(numberOfWords);

        wordLogic = WordLogic.factory(null);

    }

    public Set<DictionaryWord> generateDictionaryWithRandomWords(int numberOfWords){

        Set<DictionaryWord> words = new HashSet<>();

        for(int i = 0 ; i < numberOfWords ; i++) {

            DictionaryWord word = generateRandomWord( partsOfSpeech);
            words.add(word);
        }

        return words;
    }

    public DictionaryWord generateRandomWord( PartsOfSpeechSet partsOfSpeech ) {

        String wordSpelling;
        String wordId;

        int wordLength = DictUtil.randomInRange(2, 9);
        wordSpelling = Bangla.getWord(start, end, wordLength);
        wordId = "WD_" + UUID.randomUUID();

        DictionaryWord dictionaryWord = new DictionaryWord(wordId, wordSpelling);

        ArrayList<MeaningForPartsOfSpeech> meaningsForPartsOfSpeech = new ArrayList<>();

        for (String pos : partsOfSpeech.getPartsOfSpeeches()) {

            MeaningForPartsOfSpeech meanings = new MeaningForPartsOfSpeech();
            meanings.setType(pos);

            int numberOfMeaningForPOS = DictUtil.randomInRange(1,3);

            for(int j = 0; j < numberOfMeaningForPOS ; j++) {

                String meaning;
                wordLength = DictUtil.randomInRange(2, 9);
                meaning = Bangla.getWord(start, end, wordLength);

                String example;
                int preSentenceLen = DictUtil.randomInRange(2, 6);
                int postSentenceLen = DictUtil.randomInRange(2, 4);
                example = Bangla.getSentence(start, end, preSentenceLen, 12);
                example += " " + meaning + " ";
                example += Bangla.getSentence(start, end, postSentenceLen, 12);

                String meaningId = "MN_" + UUID.randomUUID();

                int strengh = DictUtil.randomInRange( 0 , 10);
                Meaning meaningForPOS = new Meaning(meaningId, pos, meaning, example, strengh);

                meanings.setAMeaning(meaningForPOS);

            }

            meaningsForPartsOfSpeech.add(meanings);

        }

        dictionaryWord.setMeaningForPartsOfSpeeches(meaningsForPartsOfSpeech);

        return dictionaryWord;
    }

    @Test @Ignore
    public void testBangla() {

        log.info("START!");

        int i = 0;
        for(DictionaryWord word: dictionary){
            log.info(" Word "+ i + " :" + word.toString());
            i++;
        }

    }

    @Test @Ignore
    public void storeWordTest() {

        DictionaryWord word = generateRandomWord(partsOfSpeech);

        wordLogic.saveDictionaryWord(word);

    }

    @Test @Ignore
    public void storeWords() {

        Set<DictionaryWord> words = generateDictionaryWithRandomWords(1217);
        for(DictionaryWord word:words)
            wordLogic.saveDictionaryWord(word);

    }

    @Test @Ignore
    public void searchWordsByPrefill() {

        long current_time = System.nanoTime();

        String prefix = "ক";

        List<String> results = wordLogic.searchWordsBySpelling( prefix, 10) ;

        long total_time = System.nanoTime() - current_time;

        log.info("Words for prefix: \"" + prefix + "\":" + results.toString() );
        log.info("[Total Time:" + ( total_time / 1000000.0 ) + "ms]" );

    }

    @Test @Ignore
    public void getWordBySpelling() {

        long current_time = System.nanoTime();

        String wordSpelling = "কঙঘছদঢণদ";

        DictionaryWord word = wordLogic.getDictionaryWordBySpelling(wordSpelling, null);

        long total_time = System.nanoTime() - current_time;

        if (word != null) {
            log.info("Word for spelling: \"" + wordSpelling + "\" :" + word.toString());
        } else {
            log.info("Word for spelling: \"" + wordSpelling + "\":" + "Not Found" );
        }

        log.info("[Total Time:" + (total_time / 1000000.0) + "ms]");

    }

}
