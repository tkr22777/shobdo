package IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.JsonObject;
import logics.WordLogic;
import objects.Word;
import org.junit.*;
import play.libs.Json;
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
    ArrayList<Word> createdWords;

    @Before
    public void setup() {

        log = new LogPrint(WordControllerTests.class);
        wordLogic = WordLogic.factory();
    }

    private void createWordsInDb(int numberOfWords) {

        createdWords = new ArrayList<>( DictUtil.generateRandomWordSet(numberOfWords) );
        wordLogic.createWordsBatch(createdWords); //storing for tests
    }

    @After
    public void clearSetups() {
        wordLogic.deleteAllWords();  //cleaning up for tests
        wordLogic.flushCache();
    }

    @Test
    public void simpleTest() {

        running( fakeApplication(), () -> {

            createWordsInDb(2);

            log.info("Created words: " + createdWords);

            Word word = createdWords.get(0);

            Word wordFromDb = wordLogic.getWordByWordId(word.getWordId());

            log.info("Created word retrieved from DB:" + wordFromDb);

        });

    }

    //WORD CRUD TESTS
    /*
        These API's should not know and care about the internals/logic of the system

        CREATE Test Cases:
        1. Valid object, word spelling provided, spelling do not exist, create correctly
        2. Valid object, word spelling provided, spelling exists, throw error
        3. WordId provided, throw error
        4. Meaning provided, throw error
        5. Any other attribute provided, throws error, returns reasons in the body

        GET Test Cases:
        1. Invalid wordId, throws error
        2. Valid wordId, returns correct word, word should not have meaning attribute afterJson conversion

        Update Word Test:
        1.
        2.

        Delete Word Test:
        1.
        2.

    */

    @Test
    public void createWordTest() {

        running( fakeApplication(), () -> {

            String jsonWordString = "{\n" +
                    "  \"wordId\" : null,\n" +
                    "  \"wordSpelling\" : \"ঞতটতথঙ\",\n" +
                    "  \"meaningsMap\" : { },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.toJsonNodeFromJsonString(jsonWordString);

            Result result = route( fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson) );
            assertEquals(OK, result.status());

            Word createdWord = wordLogic.getWordBySpelling("ঞতটতথঙ");

            //since request did not have an wordId and versionMeta
            createdWord.setWordId(null);
            createdWord.setVersionMeta(null);
            ObjectNode objectNode = Json.toJson(createdWord).deepCopy();
            objectNode.remove("extraMetaMap");
            objectNode.remove("versionMeta");
            Assert.assertEquals(bodyJson, JsonUtil.toJsonNodeFromJsonString(objectNode.toString()));
        });
    }

    @Test
    public void getWordByWordId() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            String wordSpelling = createdWords.get(0).getWordSpelling();
            Word word = wordLogic.getWordBySpelling(wordSpelling);

            log.info("Word:" + Json.toJson(word) );

            Assert.assertNotNull(word);
            Assert.assertNotNull(word.getWordId());

            Result result = route( fakeRequest(GET,"/api/v1/words/" + word.getWordId()) );

            assertEquals(OK, result.status());

            JsonNode wordJsonNode = JsonUtil.toJsonNodeFromJsonString(contentAsString(result));
            Assert.assertEquals(word.toString(), JsonUtil.toJsonString(wordJsonNode));
        });
    }

    @Test
    public void getWordBySpellingPost() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            String wordSpelling = createdWords.get(0).getWordSpelling();
            Word word = wordLogic.getWordBySpelling(wordSpelling);

            Assert.assertNotNull(word);
            Assert.assertNotNull(word.getWordId());

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Constants.WORD_SPELLING_KEY, word.getWordSpelling());
            JsonNode bodyJson = JsonUtil.toJsonNodeFromJsonString(jsonObject.toString());

            Result result = route( fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson) );

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
        String wordSpelling = createdWords.get(0).getWordSpelling();
        Word word = wordLogic.getWordBySpelling(wordSpelling);
        Assert.assertNotNull(word);
        Assert.assertNotNull(word.getWordId());

        Result result = route( fakeRequest(DELETE,"/api/v1/words/" + word.getWordId()) );
        assertEquals(OK, result.status());

        Assert.assertNull(wordLogic.getWordByWordId(word.getWordId()));
    }

    @Test
    public void searchWordsByPrefix() throws Exception {

        createWordsInDb(50);

        String spelling = createdWords.get(0).getWordSpelling();
        String prefix = spelling.substring(0,1);

        log.info("Test searchWordsByPrefix, prefix: " + prefix);

        Set<String> spellingsWithPrefixes = createdWords.stream()
                .filter( word -> word.getWordSpelling().startsWith(prefix) )
                .map( word-> word.getWordSpelling() )
                .collect( Collectors.toSet() );

        log.info("Spelling with prefixes:" + spellingsWithPrefixes );

        running( fakeApplication(), () -> {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Constants.SEARCH_STRING_KEY, prefix);
            JsonNode requestBodyJson = JsonUtil.toJsonNodeFromJsonString(jsonObject.toString());
            Result result = route( fakeRequest(POST,"/api/v1/words/search").bodyJson(requestBodyJson) );

            assertEquals(OK, result.status());
            JsonNode resultsJson = JsonUtil.toJsonNodeFromJsonString(contentAsString(result));
            JsonNode expectedResult = JsonUtil.toJsonNodeFromObject(spellingsWithPrefixes);
            log.info("results json:" + resultsJson);
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
