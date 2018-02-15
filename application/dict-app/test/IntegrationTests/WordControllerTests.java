package IntegrationTests;

import Exceptions.EntityDoesNotExist;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import logics.WordLogic;
import objects.Meaning;
import objects.Word;
import org.junit.*;
import play.libs.Json;
import play.mvc.Result;
import play.test.WithApplication;
import utilities.Constants;
import utilities.DictUtil;
import utilities.JsonUtil;
import utilities.LogPrint;

import java.util.*;
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
    Map<String, ArrayList<Meaning>> createdMeaningForWord;

    @Before
    public void setup() {

        log = new LogPrint(WordControllerTests.class);
        wordLogic = WordLogic.factory();
    }

    private JsonNode convertWordToJsonResponse(Word word) {

        JsonNode jsonNode = Json.toJson(word);
        List attributesToRemove = Arrays.asList("extraMetaMap", "entityMeta");
        return JsonUtil.removeFieldsFromJsonNode(jsonNode, attributesToRemove);
    }

    private void createWordsInDb(int numberOfWords) {

        createdWords = new ArrayList<>( DictUtil.generateRandomWordSet(numberOfWords) );
        wordLogic.createWords(createdWords); //storing for tests
    }

    public static JsonNode convertMeaningToResponseJNode(Meaning meaning) {

        JsonNode jsonNode = Json.toJson(meaning);
        List attributesToRemove = Arrays.asList("strength", "entityMeta");
        return JsonUtil.removeFieldsFromJsonNode(jsonNode, attributesToRemove);
    }

    private void createMeaningsInDbForWord(String wordId, String wordSpelling, int numberOfMeanings) {

        createdMeaningForWord = new HashMap<>();
        ArrayList<Meaning> meaningList = new ArrayList<>(DictUtil.generateRandomMeaning(wordSpelling, numberOfMeanings));
        wordLogic.createMeaningsBatch(wordId, meaningList);
        createdMeaningForWord.put(wordId, meaningList);
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
                    "  \"id\" : null,\n" +
                    "  \"wordSpelling\" : \"ঞতটতথঙ\",\n" +
                    "  \"meaningsMap\" : { },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);
            Result result = route( fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson) );
            assertEquals(CREATED, result.status());

            JsonNode createdJNode = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            Assert.assertEquals(bodyJson, JsonUtil.nullFieldsFromJsonNode(createdJNode, Arrays.asList("id")));

            //Making sure the data persisted
            String wordId = createdJNode.get("id").toString().replaceAll("\"","");
            JsonNode wordFromDB = convertWordToJsonResponse( wordLogic.getWordByWordId(wordId) );
            Assert.assertEquals(bodyJson, JsonUtil.nullFieldsFromJsonNode(wordFromDB, Arrays.asList("id")));
        });
    }

    @Test
    public void createWord_spellingAlreadyExits_throwsError() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            String existingWordSpelling = createdWords.get(0).getWordSpelling();
            String jsonWordString = "{\n" +
                    "  \"id\" : null,\n" +
                    "  \"wordSpelling\" : \"" + existingWordSpelling +"\",\n" +
                    "  \"meaningsMap\" : { },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);
            Result result = route( fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson) );
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.CREATE_SPELLING_EXISTS + existingWordSpelling, contentAsString(result));
        });
    }

    @Test
    public void createWord_wordIdProvided_throwsError() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            String existingWordId = createdWords.get(0).getId();

            String jsonWordString = "{\n" +
                    "  \"id\" : \"" + existingWordId + "\",\n" +
                    "  \"wordSpelling\" : \"ঞতটতথঙ\",\n" +
                    "  \"meaningsMap\" : { },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);
            Result result = route( fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson) );
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.CREATE_ID_NOT_PERMITTED + existingWordId, contentAsString(result));
        });
    }

    @Test
    public void createWord_meaningProvided_throwsError() {

        running( fakeApplication(), () -> {

            String jsonWordString = "{\n" +
                    "  \"id\" : null,\n" +
                    "  \"wordSpelling\" : \"ঞতটতথঙ\",\n" +
                    "  \"meaningsMap\" : { \"aMeaningId\":  { \"id\": \"aMeaningId\"}  },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);
            Result result = route(fakeRequest(POST, "/api/v1/words").bodyJson(bodyJson));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.MEANING_PROVIDED, contentAsString(result));
        });
    }


    /* Get tests */
    @Test
    public void getWordByWordId_validWordIdForWordInDb_wordReturned() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Result result = route( fakeRequest(GET,"/api/v1/words/" + createdWord.getId()) );
            assertEquals(OK, result.status());
            Assert.assertEquals( convertWordToJsonResponse(createdWord).toString(), contentAsString(result));
        });
    }

    @Test
    public void getWordByWordId_invalidWordId_returnedNotFound() {

        running( fakeApplication(), () -> {

            Result result = route( fakeRequest(GET,"/api/v1/words/invalid_wid" ) );
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + "invalid_wid" , contentAsString(result));
        });
    }

    @Test @Ignore //Bengali characters don't play well on API routes, route has not been implemented due to technical difficulties
    public void getWordBySpelling() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);
            Result result = route( fakeRequest(GET,"/api/v1/words/spelling" + createdWord.getWordSpelling() ) );
            assertEquals(OK, result.status());
            Assert.assertEquals( convertWordToJsonResponse(createdWord).toString(), contentAsString(result));
        });
    }

    @Test
    public void getWordBySpellingPost_existingWordSpellingForWordsInDb_wordReturned() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);
            String jsonWordString = "{" + "\"" + Constants.WORD_SPELLING_KEY + "\":\"" + createdWord.getWordSpelling() + "\"}";
            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);

            Result result = route( fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson) );
            assertEquals(OK, result.status());
            Assert.assertEquals( convertWordToJsonResponse(createdWord).toString(), contentAsString(result));
        });
    }

    @Test
    public void getWordBySpellingPost_nonExistentWordSpellingForWordsInDb_returnedNotFound() {

        running( fakeApplication(), () -> {

            String jsonWordString = "{\"" + Constants.WORD_SPELLING_KEY + "\":\"NonExistentSpelling\"}";
            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);

            Result result = route( fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson) );
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + "NonExistentSpelling" , contentAsString(result));
        });
    }

    @Test
    public void getWordBySpellingPost_invalidSpellingKey_returnedBadRequest() {

        running( fakeApplication(), () -> {

            String jsonWordString = "{\"InvalidSearchKeyField\":\"NonExistentSpelling\"}";
            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);

            Result result = route( fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson) );
            assertEquals(BAD_REQUEST, result.status());
        });
    }

    /* Update tests: Update word spelling */
    @Test
    public void updateWord_validSpellingUpdateRequest_updatedSuccessfully() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = WordLogic.deepCopyWord(createdWord);
            updateRequestWord.setWordSpelling(updateRequestWord.getWordSpelling()  + "বিবর্তিত"); //updating the spelling
            JsonNode updateRequestWordJNode = convertWordToJsonResponse(updateRequestWord);

            Result result = route( fakeRequest(PUT,"/api/v1/words/" + updateRequestWord.getId()).bodyJson(updateRequestWordJNode));
            assertEquals(OK, result.status());

            JsonNode updatedJNode = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            Assert.assertEquals(updateRequestWordJNode, updatedJNode);

            //Making sure the data persisted
            JsonNode wordFromDB = convertWordToJsonResponse( wordLogic.getWordByWordId(createdWord.getId()));
            Assert.assertEquals(updateRequestWordJNode, wordFromDB);
        });
    }

    @Test
    public void updateWord_invalidWordIdProvided_throwsError() {

        running( fakeApplication(), () -> {

            String wordId = WordLogic.generateNewWordId();
            String jsonWordString = "{\n" +
                    "  \"id\" : \"" +  wordId + "\",\n" +
                    "  \"wordSpelling\" : \"ঞতটতথঙ\",\n" +
                    "  \"meaningsMap\" : { },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);
            Result result = route( fakeRequest(PUT,"/api/v1/words/" + wordId).bodyJson(bodyJson));
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + wordId, contentAsString(result));
        });
    }

    @Test
    public void updateWord_emptySpellingProvided_throwsError() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = WordLogic.deepCopyWord(createdWord);
            updateRequestWord.setWordSpelling("");
            JsonNode updateRequestWordJNode = convertWordToJsonResponse(updateRequestWord);

            Result result = route( fakeRequest(PUT,"/api/v1/words/" + updateRequestWord.getId()).bodyJson(updateRequestWordJNode));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.WORDSPELLING_NULLOREMPTY, contentAsString(result));
        });
    }

    @Test
    public void updateWord_meaningProvided_throwsError() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = WordLogic.deepCopyWord(createdWord);
            updateRequestWord.setWordSpelling(updateRequestWord.getWordSpelling()  + "বিবর্তিত"); //updating the spelling
            Meaning meaning = new Meaning();
            meaning.setId(WordLogic.generateNewMeaningId());
            HashMap<String,Meaning> meaningHashMap = new HashMap<>();
            meaningHashMap.put(meaning.getId(), meaning);
            updateRequestWord.setMeaningsMap(meaningHashMap);
            JsonNode updateRequestWordJNode = convertWordToJsonResponse(updateRequestWord);

            Result result = route( fakeRequest(PUT,"/api/v1/words/" + updateRequestWord.getId()).bodyJson(updateRequestWordJNode));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.MEANING_PROVIDED, contentAsString(result));
        });
    }

    /* Delete Word Test: */
    @Test(expected = EntityDoesNotExist.class)
    public void deleteWord_existingWord_deletesSuccessfully() {

        createWordsInDb(1);
        String wordSpelling = createdWords.get(0).getWordSpelling();
        Word word = wordLogic.getWordBySpelling(wordSpelling);
        Assert.assertNotNull(word);
        Assert.assertNotNull(word.getId());

        Result result = route( fakeRequest(DELETE,"/api/v1/words/" + word.getId()) );
        assertEquals(OK, result.status());
        wordLogic.getWordByWordId(word.getId()); //Should throw EntityDoesNotExist exception
    }

    /* Create tests */
    @Test
    public void createMeaning_validObject_meaningCreatedCorrectly() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);

            String jsonMeaningString = "{\n" +
                    "  \"id\" : null,\n" +
                    "  \"meaning\" : \"ঢঙটধ ঙজখডঠ ঙচটঞন\",\n" +
                    "  \"partOfSpeech\" : \"অব্যয়\",\n" +
                    "  \"exampleSentence\" : \"থঞথঠঝচচতখছট খঝণঠধঙ " + word.getWordSpelling() + " ঙঞজতঢণটজঠধ \"\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonMeaningString);

            Result result = route( fakeRequest(POST,"/api/v1/words/" + word.getId() + "/meanings").bodyJson(bodyJson) );
            assertEquals(CREATED, result.status());
            JsonNode createdJNode = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            Assert.assertEquals(bodyJson, JsonUtil.nullFieldsFromJsonNode(createdJNode, Collections.singletonList("id")));

            //Making sure the data persisted
            String meaningId = createdJNode.get("id").toString().replaceAll("\"","");
            word =  wordLogic.getWordByWordId(word.getId());
            Meaning meaning = word.getMeaningsMap().get(meaningId);
            JsonNode meaningJson = convertMeaningToResponseJNode(meaning);
            Assert.assertEquals(bodyJson, JsonUtil.nullFieldsFromJsonNode(meaningJson, Collections.singletonList("id")));
        });
    }

    @Test @Ignore
    public void createMeaning_invalidWordId_throwsError() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);
            createMeaningsInDbForWord(word.getId(), word.getWordSpelling(), 2);

            /*
            createWordsInDb(1);
            String existingWordSpelling = createdWords.get(0).getWordSpelling();
            String jsonWordString = "{\n" +
                    "  \"id\" : null,\n" +
                    "  \"wordSpelling\" : \"" + existingWordSpelling +"\",\n" +
                    "  \"meaningsMap\" : { },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);
            Result result = route( fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson) );
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.CREATE_SPELLING_EXISTS + existingWordSpelling, contentAsString(result));
            */
        });
    }

    @Test @Ignore
    public void createMeaning_meaningStringIsEmpty_throwsError() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            String existingWordId = createdWords.get(0).getId();

            String jsonWordString = "{\n" +
                    "  \"id\" : \"" + existingWordId + "\",\n" +
                    "  \"wordSpelling\" : \"ঞতটতথঙ\",\n" +
                    "  \"meaningsMap\" : { },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);
            Result result = route( fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson) );
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.CREATE_ID_NOT_PERMITTED + existingWordId, contentAsString(result));
        });
    }

    /* Get tests */
    @Test @Ignore
    public void getMeaningByMeaningIdForAWord_validMeaningIdInDb_meaningReturn() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Result result = route( fakeRequest(GET,"/api/v1/words/" + createdWord.getId()) );
            assertEquals(OK, result.status());
            Assert.assertEquals( convertWordToJsonResponse(createdWord).toString(), contentAsString(result));
        });
    }

    @Test @Ignore
    public void getMeaningByMeaningIdForAWord_invalidMeaning_notFoundReturned() {

        running( fakeApplication(), () -> {

            Result result = route( fakeRequest(GET,"/api/v1/words/invalid_wid" ) );
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + "invalid_wid" , contentAsString(result));
        });
    }


    /* Update tests: Update meaning */
    @Test @Ignore
    public void updateMeaning_validMeaningRequest_updatedSuccessfully() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = WordLogic.deepCopyWord(createdWord);
            updateRequestWord.setWordSpelling(updateRequestWord.getWordSpelling()  + "বিবর্তিত"); //updating the spelling
            JsonNode updateRequestWordJNode = convertWordToJsonResponse(updateRequestWord);

            Result result = route( fakeRequest(PUT,"/api/v1/words/" + updateRequestWord.getId()).bodyJson(updateRequestWordJNode));
            assertEquals(OK, result.status());

            JsonNode updatedJNode = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            Assert.assertEquals(updateRequestWordJNode, updatedJNode);

            //Making sure the data persisted
            JsonNode wordFromDB = convertWordToJsonResponse( wordLogic.getWordByWordId(createdWord.getId()));
            Assert.assertEquals(updateRequestWordJNode, wordFromDB);
        });
    }

    @Test @Ignore
    public void updateMeaning_invalidWordIdProvided_throwsError() {

        running( fakeApplication(), () -> {

            String wordId = WordLogic.generateNewWordId();
            String jsonWordString = "{\n" +
                    "  \"id\" : \"" +  wordId + "\",\n" +
                    "  \"wordSpelling\" : \"ঞতটতথঙ\",\n" +
                    "  \"meaningsMap\" : { },\n" +
                    "  \"antonyms\" : [ ],\n" +
                    "  \"synonyms\" : [ ]\n" +
                    "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);
            Result result = route( fakeRequest(PUT,"/api/v1/words/" + wordId).bodyJson(bodyJson));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + wordId, contentAsString(result));
        });
    }

    @Test @Ignore
    public void updateMeaning_invalidMeaningIdProvided_throwsError() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = WordLogic.deepCopyWord(createdWord);
            updateRequestWord.setWordSpelling("");
            JsonNode updateRequestWordJNode = convertWordToJsonResponse(updateRequestWord);

            Result result = route( fakeRequest(PUT,"/api/v1/words/" + updateRequestWord.getId()).bodyJson(updateRequestWordJNode));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.WORDSPELLING_NULLOREMPTY, contentAsString(result));
        });
    }

    @Test @Ignore
    public void updateMeaning_emptyMeaningStringProvided_throwsError() {

        running( fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = WordLogic.deepCopyWord(createdWord);
            updateRequestWord.setWordSpelling(updateRequestWord.getWordSpelling()  + "বিবর্তিত"); //updating the spelling
            Meaning meaning = new Meaning();
            meaning.setId(WordLogic.generateNewMeaningId());
            HashMap<String,Meaning> meaningHashMap = new HashMap<>();
            meaningHashMap.put(meaning.getId(), meaning);
            updateRequestWord.setMeaningsMap(meaningHashMap);
            JsonNode updateRequestWordJNode = convertWordToJsonResponse(updateRequestWord);

            Result result = route( fakeRequest(PUT,"/api/v1/words/" + updateRequestWord.getId()).bodyJson(updateRequestWordJNode));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.MEANING_PROVIDED, contentAsString(result));
        });
    }

    /* Delete Word Test: */
    @Test @Ignore
    public void deleteMeaning() {

        createWordsInDb(1);
        String wordSpelling = createdWords.get(0).getWordSpelling();
        Word word = wordLogic.getWordBySpelling(wordSpelling);
        Assert.assertNotNull(word);
        Assert.assertNotNull(word.getId());

        Result result = route( fakeRequest(DELETE,"/api/v1/words/" + word.getId()) );
        assertEquals(OK, result.status());
        Assert.assertNull(wordLogic.getWordByWordId(word.getId()));
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
            JsonNode requestBodyJson = JsonUtil.jsonStringToJsonNode(jsonObject.toString());
            Result result = route( fakeRequest(POST,"/api/v1/words/search").bodyJson(requestBodyJson) );

            assertEquals(OK, result.status());
            JsonNode resultsJson = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            JsonNode expectedResult = JsonUtil.objectToJsonNode(spellingsWithPrefixes);
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

    @Test @Ignore
    public void tempTest() {



    }

}
