package IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import logics.WordLogic;
import objects.Word;
import org.junit.*;
import play.mvc.Result;
import play.test.WithApplication;
import utilities.Constants;
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
    ArrayList<Word> dictionary;

    @Before
    public void setup() {

        log = new LogPrint(WordControllerTests.class);
        wordLogic = WordLogic.factory();
    }

    private void createWordsInDb(int numberOfWords) {

        dictionary = new ArrayList<>( DictUtil.generateDictionaryWithRandomWords(numberOfWords) );
        wordLogic.createWordsBatch(dictionary); //storing for tests
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

            Word word = DictUtil.generateARandomWord();
            JsonNode bodyJson = JsonUtil.toJsonNodeFromObject(word);
            Result result = route( fakeRequest(POST,"/api/v1/word").bodyJson(bodyJson) );

            assertEquals(OK, result.status());

            Word createdWord = wordLogic.getWordBySpelling(word.getWordSpelling());
            createdWord.setWordId(null); //since the word to be stored did not have an wordId
            Assert.assertEquals(word.toString(), createdWord.toString());
        });
    }

    @Test
    public void getWordByWordId() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            String wordSpelling = dictionary.get(0).getWordSpelling();
            Word word = wordLogic.getWordBySpelling(wordSpelling);

            Assert.assertNotNull(word);
            Assert.assertNotNull(word.getWordId());

            Result result = route( fakeRequest(GET,"/api/v1/word/" + word.getWordId()) );

            assertEquals(OK, result.status());

            JsonNode wordJsonNode = JsonUtil.toJsonNodeFromJsonString(contentAsString(result));
            Assert.assertEquals(word.toString(), JsonUtil.toJsonString(wordJsonNode));
        });
    }

    @Test
    public void getWordBySpellingPost() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            String wordSpelling = dictionary.get(0).getWordSpelling();
            Word word = wordLogic.getWordBySpelling(wordSpelling);

            Assert.assertNotNull(word);
            Assert.assertNotNull(word.getWordId());

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Constants.WORD_SPELLING_KEY, word.getWordSpelling());
            JsonNode bodyJson = JsonUtil.toJsonNodeFromJsonString(jsonObject.toString());

            Result result = route( fakeRequest(POST,"/api/v1/word/postget").bodyJson(bodyJson) );

            assertEquals(OK, result.status());

            JsonNode wordJsonNode = JsonUtil.toJsonNodeFromJsonString(contentAsString(result));
            Assert.assertEquals(word.toString(), JsonUtil.toJsonString(wordJsonNode));
        });
    }

    @Test @Ignore
    public void updateWord() {

    }

    @Test @Ignore
    public void deleteWord() {

        createWordsInDb(1);
        String wordSpelling = dictionary.get(0).getWordSpelling();
        Word word = wordLogic.getWordBySpelling(wordSpelling);
        Assert.assertNotNull(word);
        Assert.assertNotNull(word.getWordId());

        Result result = route( fakeRequest(DELETE,"/api/v1/word/" + word.getWordId()) );
        assertEquals(OK, result.status());

        Assert.assertNull(wordLogic.getWordByWordId(word.getWordId()));
    }

    @Test
    public void searchWordsByPrefix() throws Exception {

        createWordsInDb(50);

        String spelling = dictionary.get(0).getWordSpelling();
        String prefix = spelling.substring(0,1);

        log.info("Test searchWordsByPrefix, prefix: " + prefix);

        Set<String> spellingsWithPrefixes = dictionary.stream()
                .filter( word -> word.getWordSpelling().startsWith(prefix) )
                .map( word-> word.getWordSpelling() )
                .collect( Collectors.toSet() );

        running( fakeApplication(), () -> {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Constants.SEARCH_STRING_KEY, prefix);
            JsonNode requestBodyJson = JsonUtil.toJsonNodeFromJsonString(jsonObject.toString());
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
