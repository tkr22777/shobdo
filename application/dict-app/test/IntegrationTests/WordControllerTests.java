package IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import logics.WordLogic;
import objects.Word;
import org.junit.*;
import org.junit.experimental.categories.Categories;
import play.mvc.Result;
import play.test.WithApplication;
import utilities.Constants;
import utilities.DictUtil;
import utilities.JsonUtil;
import utilities.LogPrint;

import java.util.ArrayList;
import java.util.Arrays;
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

    /* Create tests */
    @Test
    public void createWord_validObjectSpellingDoNotExitsWordIdNotProvided_wordCreatedCorrectly() {

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
            assertEquals(CREATED, result.status());
            JsonNode createdJNode = JsonUtil.toJsonNodeFromJsonString(contentAsString(result));
            Assert.assertEquals(bodyJson, JsonUtil.nullFieldsFromJsonNode(createdJNode, Arrays.asList("wordId")));
        });
    }

    @Test
    public void createWord_spellingAlreadyExits_throwsError() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            String existingWordSpelling = createdWords.get(0).getWordSpelling();
            String jsonWordString = "{\n" +
                    "  \"wordId\" : null,\n" +
                    "  \"wordSpelling\" : \"" + existingWordSpelling +"\",\n" +
                    "  \"meaningsMap\" : { },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.toJsonNodeFromJsonString(jsonWordString);
            Result result = route( fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson) );
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.CREATE_SPELLING_EXISTS + existingWordSpelling, contentAsString(result));
        });
    }

    @Test
    public void createWord_wordIdProvided_throwsError() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            String existingWordId = createdWords.get(0).getWordId();

            String jsonWordString = "{\n" +
                    "  \"wordId\" : \"" + existingWordId + "\",\n" +
                    "  \"wordSpelling\" : \"ঞতটতথঙ\",\n" +
                    "  \"meaningsMap\" : { },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.toJsonNodeFromJsonString(jsonWordString);
            Result result = route( fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson) );
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.CREATE_WORDID_EXISTS + existingWordId, contentAsString(result));
        });
    }

    @Test
    public void createWord_meaningProvided_throwsError() {

        running( fakeApplication(), () -> {

            String jsonWordString = "{\n" +
                    "  \"wordId\" : null,\n" +
                    "  \"wordSpelling\" : \"ঞতটতথঙ\",\n" +
                    "  \"meaningsMap\" : { \"aMeaningId\":  { \"meaningId\": \"aMeaningId\"}  },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.toJsonNodeFromJsonString(jsonWordString);
            Result result = route(fakeRequest(POST, "/api/v1/words").bodyJson(bodyJson));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.CREATE_MEANING_PROVIDED, contentAsString(result));
        });
    }


    /* Get tests */
    @Test
    public void getWordByWordId_validWordIdForWordInDb_wordReturned() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Result result = route( fakeRequest(GET,"/api/v1/words/" + createdWord.getWordId()) );
            assertEquals(OK, result.status());
            Assert.assertEquals( WordLogic.convertWordToResponseJNode(createdWord).toString(), contentAsString(result));
        });
    }

    @Test
    public void getWordByWordId_invalidWordId_returnedNotFound() {

        running( fakeApplication(), () -> {

            Result result = route( fakeRequest(GET,"/api/v1/words/invalid_wid" ) );
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.GET_WORD_NOT_FOUND + "invalid_wid" , contentAsString(result));
        });
    }

    @Test @Ignore //Bengali characters don't play well on API routes, route has not been implemented due to technical difficulties
    public void getWordBySpelling() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);
            Result result = route( fakeRequest(GET,"/api/v1/words/spelling" + createdWord.getWordSpelling() ) );
            assertEquals(OK, result.status());
            Assert.assertEquals( WordLogic.convertWordToResponseJNode(createdWord).toString(), contentAsString(result));
        });
    }

    @Test
    public void getWordBySpellingPost_existingWordSpellingForWordsInDb_wordReturned() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);
            String jsonWordString = "{" + "\"" + Constants.WORD_SPELLING_KEY + "\":\"" + createdWord.getWordSpelling() + "\"}";
            JsonNode bodyJson = JsonUtil.toJsonNodeFromJsonString(jsonWordString);

            Result result = route( fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson) );
            assertEquals(OK, result.status());
            Assert.assertEquals( WordLogic.convertWordToResponseJNode(createdWord).toString(), contentAsString(result));
        });
    }

    @Test
    public void getWordBySpellingPost_nonexistingWordSpellingForWordsInDb_returnedNotFound() {

        running( fakeApplication(), () -> {

            String jsonWordString = "{\"" + Constants.WORD_SPELLING_KEY + "\":\"NonExistentSpelling\"}";
            JsonNode bodyJson = JsonUtil.toJsonNodeFromJsonString(jsonWordString);

            Result result = route( fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson) );
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.GET_WORD_NOT_FOUND + "NonExistentSpelling" , contentAsString(result));
        });
    }

    @Test
    public void getWordBySpellingPost_invalidSpellingKey_returnedBadRequest() {

        running( fakeApplication(), () -> {

            String jsonWordString = "{\"InvalidSearchKeyField\":\"NonExistentSpelling\"}";
            JsonNode bodyJson = JsonUtil.toJsonNodeFromJsonString(jsonWordString);

            Result result = route( fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson) );
            assertEquals(BAD_REQUEST, result.status());
        });
    }

    /* WORD CRUD TESTS
       GET Test Cases:
       1. Invalid wordId, throws error
       2. Valid wordId, returns correct word, word should not have meaning attribute afterJson conversion
       Update Word Test:
       Delete Word Test:
    */

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
