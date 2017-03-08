package IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import logics.WordLogic;
import objects.DictionaryWord;
import objects.PartsOfSpeechSet;
import org.junit.*;
import play.mvc.Result;
import play.test.WithApplication;
import utilities.DictUtil;
import utilities.JsonUtil;
import utilities.LogPrint;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static play.test.Helpers.POST;
import static play.test.Helpers.contentAsString;

/**
 * Created by tahsinkabir on 1/7/17.
 */
public class WordControllerTests extends WithApplication {

    LogPrint log;
    WordLogic wordLogic;
    ArrayList<DictionaryWord> dictionary;

    @Before
    public void setup() {

        log = new LogPrint(WordControllerTests.class);
        wordLogic = WordLogic.factory();
    }

    private void createWordsInDb(int NUMBER_OF_WORDS) {

        dictionary = new ArrayList<>( DictUtil.generateDictionaryWithRandomWords(NUMBER_OF_WORDS) );
        wordLogic.saveDictionaryWords(dictionary); //storing for tests
    }

    @After
    public void clearSetups() {
        wordLogic.deleteAllWords();  //cleaning up for tests
        wordLogic.flushCache();
    }

    //WORD CRUD TESTS
    @Test
    public void createWordTest() {

        running( fakeApplication(), () -> {

            DictionaryWord wordToBeStored = DictUtil.generateARandomWord(new PartsOfSpeechSet());
            JsonNode bodyJson = JsonUtil.toJsonNodeFromObject(wordToBeStored);
            Result result = route( fakeRequest(POST,"/api/v1/word").bodyJson(bodyJson) );

            assertEquals(OK, result.status());
            DictionaryWord retrievedWord = wordLogic.getDictionaryWordBySpelling(wordToBeStored.getWordSpelling());
            Assert.assertEquals(wordToBeStored.toJsonString(), retrievedWord.toJsonString());
        });
    }

    @Test
    public void getWordByWordId() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            String wordSpelling = dictionary.get(0).getWordSpelling();
            DictionaryWord word = wordLogic.getDictionaryWordBySpelling(wordSpelling);

            Assert.assertNotNull(word);
            Assert.assertNotNull(word.getWordId());

            Result result = route( fakeRequest(GET,"/api/v1/word/" + word.getWordId()) );

            assertEquals(OK, result.status());

            JsonNode resultsJson = JsonUtil.toJsonNodeFromJsonString(contentAsString(result));
            Assert.assertEquals(word.toJsonString(), JsonUtil.toJsonString(resultsJson));
        });
    }

    @Test
    public void searchWordsByPrefix() throws Exception {

        createWordsInDb(300);

        String spelling = dictionary.get(0).getWordSpelling();
        String prefix = spelling.substring(0,1);

        log.info("Test searchWordsByPrefix, prefix: " + prefix);

        Set<String> spellingsWithPrefixes = dictionary.stream()
                .filter( word -> word.getWordSpelling().startsWith(prefix) )
                .map( word-> word.getWordSpelling() )
                .collect( Collectors.toSet() );

        running( fakeApplication(), () -> {

            JsonNode requestBodyJson = JsonUtil.toJsonNodeFromJsonString(
                    "{\"searchString\":\"" + prefix + "\"}");
            Result result = route( fakeRequest(POST,"/api/v1/search").bodyJson(requestBodyJson) );

            assertEquals(OK, result.status());
            JsonNode resultsJson = JsonUtil.toJsonNodeFromJsonString(contentAsString(result));
            JsonNode expectedResult = JsonUtil.toJsonNodeFromObject(spellingsWithPrefixes);
            Assert.assertEquals( expectedResult, resultsJson);
        });
    }

    @Test
    public void totalWords() {

        createWordsInDb(10);
        long totalWords = wordLogic.totalWordCount();
        Assert.assertEquals(10, totalWords);
    }
}
