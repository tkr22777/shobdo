package IntegrationTests;

import exceptions.EntityDoesNotExist;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import logics.WordLogic;
import objects.Meaning;
import objects.Word;
import org.junit.*;
import play.mvc.Result;
import play.test.WithApplication;
import objects.Constants;
import utilities.DictUtil;
import utilities.JsonUtil;
import utilities.LogPrint;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;
import static play.test.Helpers.POST;
import static play.test.Helpers.contentAsString;

/**
 * Created by Tahsin Kabir on 1/7/17.
 */
public class WordControllerTests extends WithApplication {

    LogPrint log;
    WordLogic wordLogic;
    ArrayList<Word> createdWords;
    Map<String, List<Meaning>> createdMeaningForWord;

    @Before
    public void setup() {
        log = new LogPrint(WordControllerTests.class);
        wordLogic = WordLogic.createMongoBackedWordLogic();
    }

    private void createWordsInDb(int numberOfWords) {
        createdWords = new ArrayList<>(DictUtil.generateRandomWordSet(numberOfWords));
        wordLogic.createWords(createdWords); //storing for tests
    }

    private void createMeaningsInDbForWord(String wordId, String wordSpelling, int numberOfMeanings) {
        createdMeaningForWord = new HashMap<>();
        List<Meaning> meaningList = new ArrayList<>(DictUtil.generateRandomMeaning(wordSpelling, numberOfMeanings));
        meaningList = wordLogic.createMeaningsBatch(wordId, meaningList);
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

        running(fakeApplication(), () -> {

            String jsonWordString = "{\n" +
                "  \"id\" : null,\n" +
                "  \"wordSpelling\" : \"ঞতটতথঙ\",\n" +
                "  \"meaningsMap\" : { },\n" +
                "  \"antonyms\" : [ ],\n" +
                "  \"synonyms\" : [ ]\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);
            Result result = route(fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson));
            assertEquals(CREATED, result.status());

            JsonNode createdJNode = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            Assert.assertEquals(bodyJson, JsonUtil.nullFieldsFromJsonNode(createdJNode, Arrays.asList("id")));

            //Making sure the data persisted
            String wordId = createdJNode.get("id").toString().replaceAll("\"","");
            JsonNode wordFromDB = wordLogic.getWordById(wordId).toAPIJsonNode();
            Assert.assertEquals(bodyJson, JsonUtil.nullFieldsFromJsonNode(wordFromDB, Arrays.asList("id")));
        });
    }

    @Test
    public void createWord_spellingAlreadyExits_throwsError() {

        running(fakeApplication(), () -> {

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
            Result result = route(fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.Messages.WordSpellingExists(existingWordSpelling), contentAsString(result));
        });
    }

    @Test
    public void createWord_wordIdProvided_throwsError() {

        running(fakeApplication(), () -> {

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
            Result result = route(fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.Messages.UserProvidedIdForbidden(existingWordId), contentAsString(result));
        });
    }

    @Test
    public void createWord_meaningProvided_throwsError() {

        running(fakeApplication(), () -> {

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

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Result result = route(fakeRequest(GET,"/api/v1/words/" + createdWord.getId()));
            assertEquals(OK, result.status());
            Assert.assertEquals(createdWord.toAPIJsonNode().toString(), contentAsString(result));
        });
    }

    @Test
    public void getWordByWordId_invalidWordId_returnedNotFound() {

        running(fakeApplication(), () -> {

            Result result = route(fakeRequest(GET,"/api/v1/words/invalid_wid"));
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + "invalid_wid" , contentAsString(result));
        });
    }

    @Test @Ignore //Bengali characters don't play well on API routes, route has not been implemented due to technical difficulties
    public void getWordBySpelling() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);
            Result result = route(fakeRequest(GET,"/api/v1/words/spelling" + createdWord.getWordSpelling()));
            assertEquals(OK, result.status());
            Assert.assertEquals(createdWord.toAPIJsonNode().toString(), contentAsString(result));
        });
    }

    @Test
    public void getWordBySpellingPost_existingWordSpellingForWordsInDb_wordReturned() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Constants.WORD_SPELLING_KEY, createdWord.getWordSpelling());
            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonObject.toString());

            Result result = route(fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson));
            assertEquals(OK, result.status());
            Assert.assertEquals(createdWord.toAPIJsonNode().toString(), contentAsString(result));
        });
    }

    @Test
    public void getWordBySpellingPost_nonExistentWordSpellingForWordsInDb_returnedNotFound() {

        running(fakeApplication(), () -> {

            String jsonWordString = "{\"" + Constants.WORD_SPELLING_KEY + "\":\"NonExistentSpelling\"}";
            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);

            Result result = route(fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson));
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + "NonExistentSpelling" , contentAsString(result));
        });
    }

    @Test
    public void getWordBySpellingPost_invalidSpellingKey_returnedBadRequest() {

        running(fakeApplication(), () -> {

            String jsonWordString = "{\"InvalidSearchKeyField\":\"NonExistentSpelling\"}";
            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);

            Result result = route(fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson));
            assertEquals(BAD_REQUEST, result.status());
        });
    }

    /* Update tests: Update word spelling */
    @Test
    public void updateWord_validSpellingUpdateRequest_updatedSuccessfully() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = WordLogic.deepCopyWord(createdWord);
            updateRequestWord.setWordSpelling(updateRequestWord.getWordSpelling()  + "বিবর্তিত"); //updating the spelling
            JsonNode updateRequestWordJNode = updateRequestWord.toAPIJsonNode();

            Result result = route(fakeRequest(PUT,"/api/v1/words/" + updateRequestWord.getId()).bodyJson(updateRequestWordJNode));
            assertEquals(OK, result.status());

            JsonNode updatedJNode = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            Assert.assertEquals(updateRequestWordJNode, updatedJNode);

            //Making sure the data persisted
            JsonNode wordFromDB = wordLogic.getWordById(createdWord.getId()).toAPIJsonNode();
            Assert.assertEquals(updateRequestWordJNode, wordFromDB);
        });
    }

    @Test
    public void updateWord_invalidWordIdProvided_throwsError() {

        running(fakeApplication(), () -> {

            String wordId = "TestWordId";
            String jsonWordString = "{\n" +
                "  \"id\" : \"" +  wordId + "\",\n" +
                "  \"wordSpelling\" : \"ঞতটতথঙ\",\n" +
                "  \"meaningsMap\" : { },\n" +
                "  \"antonyms\" : [ ],\n" +
                "  \"synonyms\" : [ ]\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonWordString);
            Result result = route(fakeRequest(PUT,"/api/v1/words/" + wordId).bodyJson(bodyJson));
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + wordId, contentAsString(result));
        });
    }

    @Test
    public void updateWord_emptySpellingProvided_throwsError() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = WordLogic.deepCopyWord(createdWord);
            updateRequestWord.setWordSpelling("");
            JsonNode updateRequestWordJNode = updateRequestWord.toAPIJsonNode();

            Result result = route(fakeRequest(PUT,"/api/v1/words/" + updateRequestWord.getId()).bodyJson(updateRequestWordJNode));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.WORDSPELLING_NULLOREMPTY, contentAsString(result));
        });
    }

    @Test
    public void updateWord_meaningProvided_throwsError() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = WordLogic.deepCopyWord(createdWord);
            updateRequestWord.setWordSpelling(updateRequestWord.getWordSpelling()  + "বিবর্তিত"); //updating the spelling
            Meaning meaning = Meaning.builder()
                .id(WordLogic.generateNewMeaningId())
                .build();
            updateRequestWord.addMeaningToWord(meaning);
            JsonNode updateRequestWordJNode = updateRequestWord.toAPIJsonNode();

            Result result = route(fakeRequest(PUT,"/api/v1/words/" + updateRequestWord.getId()).bodyJson(updateRequestWordJNode));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.MEANING_PROVIDED, contentAsString(result));
        });
    }

    /* Delete Word Test: */
    @Test(expected = EntityDoesNotExist.class)
    public void deleteWord_existingWord_deletesSuccessfully() throws IOException {

        createWordsInDb(1);
        String wordSpelling = createdWords.get(0).getWordSpelling();
        Word word = wordLogic.getWordBySpelling(wordSpelling);
        Assert.assertNotNull(word);
        Assert.assertNotNull(word.getId());

        Result result = route(fakeRequest(DELETE,"/api/v1/words/" + word.getId()));
        assertEquals(OK, result.status());
        wordLogic.getWordById(word.getId()); //Should throw EntityDoesNotExist exception
    }

    /* Create tests */
    @Test
    public void createMeaning_validObject_meaningCreatedCorrectly() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);

            String jsonMeaningString = "{\n" +
                "  \"id\" : null,\n" +
                "  \"meaning\" : \"ঢঙটধ ঙজখডঠ ঙচটঞন\",\n" +
                "  \"partOfSpeech\" : \"অব্যয়\",\n" +
                "  \"strength\" : 0,\n" +
                "  \"exampleSentence\" : \"থঞথঠঝচচতখছট খঝণঠধঙ " + word.getWordSpelling() + " ঙঞজতঢণটজঠধ \"\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonMeaningString);

            Result result = route(fakeRequest(POST,"/api/v1/words/" + word.getId() + "/meanings").bodyJson(bodyJson));
            assertEquals(CREATED, result.status());
            JsonNode createdJNode = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            Assert.assertEquals(bodyJson, JsonUtil.nullFieldsFromJsonNode(createdJNode, Collections.singletonList("id")));

            //Making sure the data persisted
            String meaningId = createdJNode.get("id").toString().replaceAll("\"","");
            word =  wordLogic.getWordById(word.getId());
            Meaning meaning = word.getMeaningsMap().get(meaningId);
            JsonNode meaningJson = meaning.toAPIJsonNode();
            Assert.assertEquals(bodyJson, JsonUtil.nullFieldsFromJsonNode(meaningJson, Collections.singletonList("id")));
        });
    }

    @Test
    public void createMeaning_invalidWordId_throwsError() {

        running(fakeApplication(), () -> {

            String jsonMeaningString = "{\n" +
                "  \"id\" : null,\n" +
                "  \"meaning\" : \"ঢঙটধ ঙজখডঠ ঙচটঞন\",\n" +
                "  \"partOfSpeech\" : \"অব্যয়\",\n" +
                "  \"strength\" : 0,\n" +
                "  \"exampleSentence\" : \"থঞথঠঝচচতখছট খঝণঠধঙ ঙঞজতঢণটজঠধ \"\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonMeaningString);

            Result result = route(fakeRequest(POST,"/api/v1/words/" + "invalidId" + "/meanings").bodyJson(bodyJson));
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + "invalidId", contentAsString(result));
        });
    }

    @Test
    public void createMeaning_meaningStringIsEmpty_throwsError() {

        running(fakeApplication(), () -> {

            String jsonMeaningString = "{\n" +
                "  \"id\" : null,\n" +
                "  \"meaning\" : \"\",\n" +
                "  \"partOfSpeech\" : \"অব্যয়\",\n" +
                "  \"strength\" : 0,\n" +
                "  \"exampleSentence\" : \"থঞথঠঝচচতখছট খঝণঠধঙ ঙঞজতঢণটজঠধ \"\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode(jsonMeaningString);

            Result result = route(fakeRequest(POST,"/api/v1/words/someWordId/meanings")
                .bodyJson(bodyJson));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.MEANING_NULLOREMPTY, contentAsString(result));
        });
    }

    /* Get tests */
    @Test
    public void getMeaningByMeaningIdForAWord_validMeaningIdInDb_meaningReturnedSuccessfully() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);
            createMeaningsInDbForWord(createdWord.getId(), createdWord.getWordSpelling(), 1);
            Meaning meaning = createdMeaningForWord.get(createdWord.getId()).get(0);

            Result result = route(fakeRequest(GET, "/api/v1/words/" + createdWord.getId() + "/meanings/" + meaning.getId()));
            assertEquals(OK, result.status());
            Assert.assertEquals(meaning.toAPIJsonNode().toString(), contentAsString(result));
        });
    }

    @Test
    public void getMeaningByMeaningIdForAWord_invalidMeaningId_notFoundReturned() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Result result = route(fakeRequest(GET, "/api/v1/words/" + createdWord.getId() + "/meanings/invalidId"));
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + "invalidId" , contentAsString(result));
        });
    }

    /* Update tests: Update meaning */
    @Test
    public void updateMeaning_validMeaningRequest_updatedSuccessfully() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);
            createMeaningsInDbForWord(word.getId(), word.getWordSpelling(), 1);
            Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

            Meaning meaningRequest = WordLogic.deepCopyMeaning(meaning);
            meaningRequest.setMeaning(meaningRequest.getMeaning()  + "বিবর্তিত"); //updating the meaning
            JsonNode meaningRequestJNode = meaningRequest.toAPIJsonNode();

            String route = "/api/v1/words/" + word.getId() + "/meanings/" + meaningRequest.getId() ;
            Result result = route(fakeRequest(PUT,route).bodyJson(meaningRequestJNode));
            assertEquals(OK, result.status());

            JsonNode updatedJNode = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            Assert.assertEquals(meaningRequestJNode, updatedJNode);

            //Making sure the data persisted
            String meaningId = updatedJNode.get("id").toString().replaceAll("\"","");
            word =  wordLogic.getWordById(word.getId());
            meaning = word.getMeaningsMap().get(meaningId);
            Assert.assertEquals(updatedJNode, meaning.toAPIJsonNode());
        });
    }

    @Test
    public void updateMeaning_invalidWordIdProvided_throwsError() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);
            createMeaningsInDbForWord(word.getId(), word.getWordSpelling(), 1);
            Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

            Meaning meaningRequest = WordLogic.deepCopyMeaning(meaning);
            meaningRequest.setMeaning(meaningRequest.getMeaning()  + "বিবর্তিত"); //updating the meaning
            JsonNode meaningRequestJNode = meaningRequest.toAPIJsonNode();

            String route = "/api/v1/words/invalidWordId/meanings/" + meaningRequest.getId() ;
            Result result = route(fakeRequest(PUT,route).bodyJson(meaningRequestJNode));
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + "invalidWordId", contentAsString(result));
        });
    }

    @Test
    public void updateMeaning_invalidMeaningIdProvided_throwsError() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);
            createMeaningsInDbForWord(word.getId(), word.getWordSpelling(), 1);
            Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

            Meaning meaningRequest = WordLogic.deepCopyMeaning(meaning);
            meaningRequest.setMeaning(meaningRequest.getMeaning()  + "বিবর্তিত"); //updating the meaning
            meaningRequest.setId("invalidMeaningId");
            JsonNode meaningRequestJNode = meaningRequest.toAPIJsonNode();

            String route = "/api/v1/words/" + word.getId() + "/meanings/" + meaningRequest.getId() ;
            Result result = route(fakeRequest(PUT,route).bodyJson(meaningRequestJNode));
            assertEquals(NOT_FOUND, result.status());
            assertEquals(Constants.ENTITY_NOT_FOUND + "invalidMeaningId", contentAsString(result));
        });
    }

    @Test
    public void updateMeaning_emptyMeaningStringProvided_throwsError() {

        running(fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);
            createMeaningsInDbForWord(word.getId(), word.getWordSpelling(), 1);
            Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

            Meaning meaningRequest = WordLogic.deepCopyMeaning(meaning);
            meaningRequest.setMeaning("");
            JsonNode meaningRequestJNode = meaningRequest.toAPIJsonNode();

            String route = "/api/v1/words/" + word.getId() + "/meanings/" + meaningRequest.getId() ;
            Result result = route(fakeRequest(PUT,route).bodyJson(meaningRequestJNode));
            assertEquals(BAD_REQUEST, result.status());
            assertEquals(Constants.MEANING_NULLOREMPTY, contentAsString(result));
        });
    }

    /* Delete Word Test: */
    @Test
    public void deleteMeaning_validMeaningId_meaningDeletedCorrectly() {

        createWordsInDb(1);
        Word word = createdWords.get(0);
        createMeaningsInDbForWord(word.getId(), word.getWordSpelling(), 1);
        Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

        Assert.assertNotNull(meaning);
        Assert.assertNotNull(meaning.getId());

        Result result = route(fakeRequest(DELETE,"/api/v1/words/" + word.getId() + "/meanings/" + meaning.getId()));
        assertEquals(OK, result.status());
        Assert.assertNull(wordLogic.getWordById(word.getId()).getMeaningsMap().get(meaning.getId()));
    }

    /* Delete Word Test: */
    @Test
    public void deleteMeaning_invalidWordId_returnsNotFound() {

        createWordsInDb(1);
        Word word = createdWords.get(0);
        createMeaningsInDbForWord(word.getId(), word.getWordSpelling(), 1);
        Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

        Assert.assertNotNull(meaning);
        Assert.assertNotNull(meaning.getId());

        Result result = route(fakeRequest(DELETE,"/api/v1/words/" + "invalidWordId" + "/meanings/" + meaning.getId()));
        assertEquals(NOT_FOUND, result.status());
        assertEquals(Constants.ENTITY_NOT_FOUND + "invalidWordId", contentAsString(result));
    }

    @Test
    public void searchWordsByPrefix() throws Exception {

        createWordsInDb(50);

        String spelling = createdWords.get(0).getWordSpelling();
        String prefix = spelling.substring(0,1);

        log.info("Test searchWordsByPrefix, prefix: " + prefix);

        Set<String> spellingsWithPrefixes = createdWords.stream()
            .filter(word -> word.getWordSpelling().startsWith(prefix))
            .map(word-> word.getWordSpelling())
            .collect(Collectors.toSet());

        log.info("Spelling with prefixes:" + spellingsWithPrefixes);

        running(fakeApplication(), () -> {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Constants.SEARCH_STRING_KEY, prefix);
            JsonNode requestBodyJson = JsonUtil.jsonStringToJsonNode(jsonObject.toString());
            Result result = route(fakeRequest(POST,"/api/v1/words/search").bodyJson(requestBodyJson));

            assertEquals(OK, result.status());
            JsonNode resultsJson = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            JsonNode expectedResult = JsonUtil.objectToJsonNode(spellingsWithPrefixes);
            log.info("results json:" + resultsJson);
            Assert.assertEquals(expectedResult, resultsJson);
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
