package integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import common.stores.MongoStoreFactory;
import exceptions.EntityDoesNotExist;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import utilities.Constants;
import utilities.JsonUtil;
import utilities.ShobdoLogger;
import utilities.TestUtil;
import word.caches.WordCache;
import word.WordLogic;
import word.stores.WordStoreMongoImpl;
import word.objects.Meaning;
import word.objects.Word;

import java.util.*;
import java.util.stream.Collectors;

import static play.test.Helpers.POST;
import static play.test.Helpers.contentAsString;

public class WordControllerTests extends WithApplication {

    private final ShobdoLogger log;
    private final WordLogic wordLogic;
    private final ArrayList<Word> createdWords;
    private final Map<String, List<Meaning>> createdMeaningForWord;

    public WordControllerTests() {
        log = new ShobdoLogger(WordControllerTests.class);
        WordStoreMongoImpl storeMongo = new WordStoreMongoImpl(MongoStoreFactory.getWordCollection());
        wordLogic = new WordLogic(storeMongo, WordCache.getCache());
        createdWords = new ArrayList<>();
        createdMeaningForWord = new HashMap<>();
    }

    @Before
    public void setup() {
    }

    private void createWordsInDb(int numberOfWords) {
        createdWords.addAll(TestUtil.generateRandomWordSet(numberOfWords));
        createdWords.forEach(wordLogic::createWord);
    }

    private void createMeaningsInDbForWord(String wordId, String spelling, int numberOfMeanings) {
        List<Meaning> meaningList = new ArrayList<>(TestUtil.generateMeanings(spelling, numberOfMeanings));
        meaningList = meaningList.stream()
            .map(meaning -> wordLogic.createMeaning(wordId, meaning))
            .collect(Collectors.toList());
        createdMeaningForWord.put(wordId, meaningList);
    }

    @After
    public void clearSetups() {
        createdWords.clear();
        createdMeaningForWord.clear();
        wordLogic.deleteAllWords();  //cleaning up for tests
        wordLogic.flushCache();
    }

    /* Create tests */
    @Test
    public void createWord_validObjectSpellingDoesNotExistWordIdNotProvided_wordCreatedCorrectly() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            String jsonWordString = "{\n" +
                "  \"id\" : null,\n" +
                "  \"spelling\" : \"ঞতটতথঙ\",\n" +
                "  \"meanings\" : { },\n" +
                "  \"antonyms\" : [ ],\n" +
                "  \"synonyms\" : [ ]\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jStringToJNode(jsonWordString);
            Result result = Helpers.route(Helpers.fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson));
            Assert.assertEquals(Helpers.CREATED, result.status());

            JsonNode createdJNode = JsonUtil.jStringToJNode(contentAsString(result));
            Assert.assertEquals(bodyJson, JsonUtil.nullFields(createdJNode, Arrays.asList("id")));

            //Making sure the data persisted
            String wordId = createdJNode.get("id").toString().replaceAll("\"","");
            JsonNode wordFromDB = wordLogic.getWordById(wordId).jsonNode();
            Assert.assertEquals(bodyJson, JsonUtil.nullFields(wordFromDB, Arrays.asList("id")));
        });
    }

    @Test
    public void createWord_spellingAlreadyExits_throwsError() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            String existingSpelling = createdWords.get(0).getSpelling();
            String jsonWordString = "{\n" +
                "  \"id\" : null,\n" +
                "  \"spelling\" : \"" + existingSpelling +"\",\n" +
                "  \"meanings\" : { },\n" +
                "  \"antonyms\" : [ ],\n" +
                "  \"synonyms\" : [ ]\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jStringToJNode(jsonWordString);
            Result result = Helpers.route(Helpers.fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson));
            Assert.assertEquals(Helpers.BAD_REQUEST, result.status());
            Assert.assertEquals(Constants.Messages.SpellingExists(existingSpelling), contentAsString(result));
        });
    }

    @Test
    public void createWord_wordIdProvided_throwsError() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            String wordId = "SOME_GIVEN_WD_ID";

            String jsonWordString = "{\n" +
                "  \"id\" : \"" + wordId + "\",\n" +
                "  \"spelling\" : \"ঞতটতথঙ\",\n" +
                "  \"meanings\" : { },\n" +
                "  \"antonyms\" : [ ],\n" +
                "  \"synonyms\" : [ ]\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jStringToJNode(jsonWordString);
            Result result = Helpers.route(Helpers.fakeRequest(POST,"/api/v1/words").bodyJson(bodyJson));
            Assert.assertEquals(Helpers.BAD_REQUEST, result.status());
            Assert.assertEquals(Constants.Messages.UserProvidedIdForbidden(wordId), contentAsString(result));
        });
    }

    @Test
    public void createWord_meaningProvided_throwsError() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            String jsonWordString = "{\n" +
                "  \"id\" : null,\n" +
                "  \"spelling\" : \"ঞতটতথঙ\",\n" +
                "  \"meanings\" : { \"aMeaningId\":  { \"id\": \"aMeaningId\"}  },\n" +
                "  \"antonyms\" : [ ],\n" +
                "  \"synonyms\" : [ ]\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jStringToJNode(jsonWordString);
            Result result = Helpers.route(Helpers.fakeRequest(POST, "/api/v1/words").bodyJson(bodyJson));
            Assert.assertEquals(Helpers.BAD_REQUEST, result.status());
            Assert.assertEquals(Constants.MESSAGES_MEANING_PROVIDED, contentAsString(result));
        });
    }

    /* Get tests */
    @Test
    public void getWordByWordId_validWordIdForWordInDb_wordReturned() {
        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Result result = Helpers.route(Helpers.fakeRequest(Helpers.GET,"/api/v1/words/" + createdWord.getId()));
            Assert.assertEquals(Helpers.OK, result.status());
            Assert.assertEquals(createdWord.jsonNode().toString(), contentAsString(result));
        });
    }

    @Test
    public void getWordByWordId_invalidWordId_returnedNotFound() {
        Helpers.running(Helpers.fakeApplication(), () -> {
            Result result = Helpers.route(Helpers.fakeRequest(Helpers.GET,"/api/v1/words/invalid_wid"));
            Assert.assertEquals(Helpers.NOT_FOUND, result.status());
            Assert.assertEquals(Constants.Messages.EntityNotFound("invalid_wid"), contentAsString(result));
        });
    }

    //Bengali characters do not work on the API routes, thus POST body based word retrieval
    @Test
    public void getWordBySpellingPost_existingSpellingForWordsInDb_wordReturned() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Constants.KEY_SPELLING, createdWord.getSpelling());
            JsonNode bodyJson = JsonUtil.jStringToJNode(jsonObject.toString());

            Result result = Helpers.route(
                Helpers.fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson));
            Assert.assertEquals(Helpers.OK, result.status());
            Assert.assertEquals(createdWord.jsonNode().toString(), contentAsString(result));
        });
    }

    @Test
    public void getWordBySpellingPost_nonExistentSpellingForWordsInDb_returnedNotFound() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            String jsonWordString = "{\"" + Constants.KEY_SPELLING + "\":\"NonExistentSpelling\"}";
            JsonNode bodyJson = JsonUtil.jStringToJNode(jsonWordString);

            Result result = Helpers.route(
                Helpers.fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson));
            Assert.assertEquals(Helpers.NOT_FOUND, result.status());
            Assert.assertEquals(Constants.Messages.EntityNotFound( "NonExistentSpelling"), contentAsString(result));
        });
    }

    @Test
    public void getWordBySpellingPost_invalidSpellingKey_returnedBadRequest() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            String jsonWordString = "{\"InvalidSearchKeyField\":\"NonExistentSpelling\"}";
            JsonNode bodyJson = JsonUtil.jStringToJNode(jsonWordString);

            Result result = Helpers.route(
                Helpers.fakeRequest(POST,"/api/v1/words/postget").bodyJson(bodyJson));
            Assert.assertEquals(Helpers.BAD_REQUEST, result.status());
        });
    }

    /* Update tests: Update word spelling */
    @Test
    public void updateWord_validSpellingUpdateRequest_updatedSuccessfully() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = Word.fromWord(createdWord);
            updateRequestWord.setSpelling(updateRequestWord.getSpelling() + "বিবর্তিত"); //updating the spelling
            JsonNode updateRequestWordJNode = updateRequestWord.jsonNode();

            Result result = Helpers.route(Helpers.fakeRequest(
                Helpers.PUT, String.format("/api/v1/words/%s", updateRequestWord.getId()))
                .bodyJson(updateRequestWordJNode));
            Assert.assertEquals(Helpers.OK, result.status());

            JsonNode updatedJNode = JsonUtil.jStringToJNode(contentAsString(result));
            Assert.assertEquals(updateRequestWordJNode, updatedJNode);

            //Making sure the data persisted
            JsonNode wordFromDB = wordLogic.getWordById(createdWord.getId()).jsonNode();
            Assert.assertEquals(updateRequestWordJNode, wordFromDB);
        });
    }

    @Test
    public void updateWord_invalidWordIdProvided_throwsError() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            String wordId = "TestWordId";
            String jsonWordString = "{\n" +
                "  \"id\" : \"" +  wordId + "\",\n" +
                "  \"spelling\" : \"ঞতটতথঙ\",\n" +
                "  \"meanings\" : { },\n" +
                "  \"antonyms\" : [ ],\n" +
                "  \"synonyms\" : [ ]\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jStringToJNode(jsonWordString);
            Result result = Helpers.route(
                Helpers.fakeRequest(Helpers.PUT,"/api/v1/words/" + wordId)
                    .bodyJson(bodyJson));
            Assert.assertEquals(Helpers.NOT_FOUND, result.status());
            Assert.assertEquals(Constants.Messages.EntityNotFound(wordId), contentAsString(result));
        });
    }

    @Test
    public void updateWord_emptySpellingProvided_throwsError() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = Word.fromWord(createdWord);
            updateRequestWord.setSpelling("");
            JsonNode updateRequestWordJNode = updateRequestWord.jsonNode();

            Result result = Helpers.route(Helpers.fakeRequest(Helpers.PUT,"/api/v1/words/" + updateRequestWord.getId()).bodyJson(updateRequestWordJNode));
            Assert.assertEquals(Helpers.BAD_REQUEST, result.status());
            Assert.assertEquals(Constants.MESSAGES_SPELLING_NULLOREMPTY, contentAsString(result));
        });
    }

    @Test
    public void updateWord_meaningProvided_throwsError() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);

            Word updateRequestWord = Word.fromWord(createdWord);
            updateRequestWord.setSpelling(updateRequestWord.getSpelling() + "বিবর্তিত"); //updating the spelling
            Meaning meaning = Meaning.builder()
                .id("aMeaningId")
                .build();
            updateRequestWord.addMeaningToWord(meaning);
            JsonNode updateRequestWordJNode = updateRequestWord.jsonNode();

            Result result = Helpers.route(Helpers.fakeRequest(Helpers.PUT,"/api/v1/words/" + updateRequestWord.getId()).bodyJson(updateRequestWordJNode));
            Assert.assertEquals(Helpers.BAD_REQUEST, result.status());
            Assert.assertEquals(Constants.MESSAGES_MEANING_PROVIDED, contentAsString(result));
        });
    }

    /* Delete Word Test */
    @Test(expected = EntityDoesNotExist.class)
    public void deleteWord_existingWord_deletesSuccessfully() {

        createWordsInDb(1);
        String spelling = createdWords.get(0).getSpelling();
        Word word = wordLogic.getWordBySpelling(spelling);
        Assert.assertNotNull(word);
        Assert.assertNotNull(word.getId());

        Result result = Helpers.route(Helpers.fakeRequest(Helpers.DELETE,"/api/v1/words/" + word.getId()));
        Assert.assertEquals(Helpers.OK, result.status());
        wordLogic.getWordById(word.getId()); //Should throw EntityDoesNotExist exception
    }

    /* Create meaning tests */
    @Test
    public void createMeaning_validObject_meaningCreatedCorrectly() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);

            String jsonMeaningString = "{\n" +
                "  \"id\" : null,\n" +
                "  \"meaning\" : \"ঢঙটধ ঙজখডঠ ঙচটঞন\",\n" +
                "  \"strength\" : 0,\n" +
                "  \"partOfSpeech\" : \"অব্যয়\",\n" +
                "  \"pronunciation\" : null,\n" +
                "  \"exampleSentence\" : \"থঞথঠঝচচতখছট খঝণঠধঙ " + word.getSpelling() + " ঙঞজতঢণটজঠধ \"\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jStringToJNode(jsonMeaningString);
            Result result = Helpers.route(Helpers.fakeRequest(POST,"/api/v1/words/" + word.getId() + "/meanings")
                .bodyJson(bodyJson));
            Assert.assertEquals(Helpers.CREATED, result.status());
            JsonNode createdJNode = JsonUtil.jStringToJNode(contentAsString(result));
            Assert.assertEquals(bodyJson, JsonUtil.nullFields(createdJNode, Arrays.asList("id")));

            //Making sure the data persisted
            String meaningId = createdJNode.get("id").toString().replaceAll("\"","");
            word = wordLogic.getWordById(word.getId());
            Meaning meaning = word.getMeanings().get(meaningId);
            Assert.assertEquals(bodyJson, JsonUtil.nullFields(meaning.jsonNode(), Arrays.asList("id")));
        });
    }

    @Test
    public void createMeaning_invalidWordId_throwsError() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            String jsonMeaningString = "{\n" +
                "  \"id\" : null,\n" +
                "  \"meaning\" : \"ঢঙটধ ঙজখডঠ ঙচটঞন\",\n" +
                "  \"partOfSpeech\" : \"অব্যয়\",\n" +
                "  \"strength\" : 0,\n" +
                "  \"exampleSentence\" : \"থঞথঠঝচচতখছট খঝণঠধঙ ঙঞজতঢণটজঠধ \"\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jStringToJNode(jsonMeaningString);

            Result result = Helpers.route(Helpers.fakeRequest(POST,"/api/v1/words/invalidId/meanings")
                .bodyJson(bodyJson));
            Assert.assertEquals(Helpers.NOT_FOUND, result.status());
            Assert.assertEquals(Constants.Messages.EntityNotFound( "invalidId"), contentAsString(result));
        });
    }

    @Test
    public void createMeaning_meaningStringIsEmpty_throwsError() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            String jsonMeaningString = "{\n" +
                "  \"id\" : null,\n" +
                "  \"meaning\" : \"\",\n" +
                "  \"partOfSpeech\" : \"অব্যয়\",\n" +
                "  \"strength\" : 0,\n" +
                "  \"exampleSentence\" : \"থঞথঠঝচচতখছট খঝণঠধঙ ঙঞজতঢণটজঠধ \"\n" +
            "}";

            JsonNode bodyJson = JsonUtil.jStringToJNode(jsonMeaningString);

            Result result = Helpers.route(Helpers.fakeRequest(POST,"/api/v1/words/someWordId/meanings")
                .bodyJson(bodyJson));
            Assert.assertEquals(Helpers.BAD_REQUEST, result.status());
            Assert.assertEquals(Constants.MESSAGES_MEANING_NULLOREMPTY, contentAsString(result));
        });
    }

    /* Get meaning tests */
    @Test
    public void getMeaningByMeaningIdForAWord_validMeaningIdInDb_meaningReturnedSuccessfully() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word createdWord = createdWords.get(0);
            createMeaningsInDbForWord(createdWord.getId(), createdWord.getSpelling(), 1);
            Meaning meaning = createdMeaningForWord.get(createdWord.getId()).get(0);

            Result result = Helpers.route(Helpers.fakeRequest(
                Helpers.GET, "/api/v1/words/" + createdWord.getId() + "/meanings/" + meaning.getId()));
            Assert.assertEquals(Helpers.OK, result.status());
            Assert.assertEquals(meaning.jsonNode().toString(), contentAsString(result));
        });
    }

    @Test
    public void getMeaningByMeaningIdForAWord_invalidMeaningId_notFoundReturned() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);

            Result result = Helpers.route(Helpers.fakeRequest(Helpers.GET, "/api/v1/words/" + word.getId() + "/meanings/invalidId"));
            Assert.assertEquals(Helpers.NOT_FOUND, result.status());
            Assert.assertEquals(Constants.Messages.EntityNotFound( "invalidId"), contentAsString(result));
        });
    }

    /* Update tests: Update meaning */
    @Test
    public void updateMeaning_validMeaningRequest_updatedSuccessfully() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);
            createMeaningsInDbForWord(word.getId(), word.getSpelling(), 1);
            Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

            Meaning meaningRequest = Meaning.fromMeaning(meaning);
            meaningRequest.setMeaning(meaningRequest.getMeaning()  + "বিবর্তিত"); //updating the meaning
            JsonNode meaningRequestJNode = meaningRequest.jsonNode();

            String route = "/api/v1/words/" + word.getId() + "/meanings/" + meaningRequest.getId() ;
            Result result = Helpers.route(Helpers.fakeRequest(Helpers.PUT,route).bodyJson(meaningRequestJNode));
            Assert.assertEquals(Helpers.OK, result.status());

            JsonNode updatedJNode = JsonUtil.jStringToJNode(contentAsString(result));
            Assert.assertEquals(meaningRequestJNode, updatedJNode);

            //Making sure the data persisted
            String meaningId = updatedJNode.get("id").toString().replaceAll("\"","");
            word =  wordLogic.getWordById(word.getId());
            meaning = word.getMeanings().get(meaningId);
            Assert.assertEquals(updatedJNode, meaning.jsonNode());
        });
    }

    @Test
    public void updateMeaning_invalidWordIdProvided_throwsError() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);
            createMeaningsInDbForWord(word.getId(), word.getSpelling(), 1);
            Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

            Meaning meaningRequest = Meaning.fromMeaning(meaning);
            meaningRequest.setMeaning(meaningRequest.getMeaning()  + "বিবর্তিত"); //updating the meaning
            JsonNode meaningRequestJNode = meaningRequest.jsonNode();

            String route = "/api/v1/words/invalidWordId/meanings/" + meaningRequest.getId() ;
            Result result = Helpers.route(Helpers.fakeRequest(Helpers.PUT,route).bodyJson(meaningRequestJNode));
            Assert.assertEquals(Helpers.NOT_FOUND, result.status());
            Assert.assertEquals(Constants.Messages.EntityNotFound( "invalidWordId"), contentAsString(result));
        });
    }

    @Test
    public void updateMeaning_invalidMeaningIdProvided_throwsError() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);
            createMeaningsInDbForWord(word.getId(), word.getSpelling(), 1);
            Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

            Meaning meaningRequest = Meaning.fromMeaning(meaning);
            meaningRequest.setMeaning(meaningRequest.getMeaning()  + "বিবর্তিত"); //updating the meaning
            meaningRequest.setId("invalidMeaningId");
            JsonNode meaningRequestJNode = meaningRequest.jsonNode();

            String route = "/api/v1/words/" + word.getId() + "/meanings/" + meaningRequest.getId() ;
            Result result = Helpers.route(Helpers.fakeRequest(Helpers.PUT,route).bodyJson(meaningRequestJNode));
            Assert.assertEquals(Helpers.NOT_FOUND, result.status());
            Assert.assertEquals(Constants.Messages.EntityNotFound( "invalidMeaningId"), contentAsString(result));
        });
    }

    @Test
    public void updateMeaning_emptyMeaningStringProvided_throwsError() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);
            createMeaningsInDbForWord(word.getId(), word.getSpelling(), 1);
            Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

            Meaning meaningRequest = Meaning.fromMeaning(meaning);
            meaningRequest.setMeaning("");
            JsonNode meaningRequestJNode = meaningRequest.jsonNode();

            String route = "/api/v1/words/" + word.getId() + "/meanings/" + meaningRequest.getId() ;
            Result result = Helpers.route(Helpers.fakeRequest(Helpers.PUT,route).bodyJson(meaningRequestJNode));
            Assert.assertEquals(Helpers.BAD_REQUEST, result.status());
            Assert.assertEquals(Constants.MESSAGES_MEANING_NULLOREMPTY, contentAsString(result));
        });
    }

    /* Delete Meaning Test */
    @Test(expected = EntityDoesNotExist.class)
    public void deleteMeaning_validMeaningId_meaningDeletedCorrectly() {

        createWordsInDb(1);
        Word word = createdWords.get(0);
        createMeaningsInDbForWord(word.getId(), word.getSpelling(), 1);
        Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

        Assert.assertNotNull(meaning);
        Assert.assertNotNull(meaning.getId());

        Result result = Helpers.route(Helpers.fakeRequest(Helpers.DELETE,"/api/v1/words/" + word.getId() + "/meanings/" + meaning.getId()));
        Assert.assertEquals(Helpers.OK, result.status());
        wordLogic.getMeaning(word.getId(), meaning.getId()); //Should throw EntityDoesNotExist exception
    }

    @Test
    public void deleteMeaning_invalidWordId_returnsNotFound() {

        createWordsInDb(1);
        Word word = createdWords.get(0);
        createMeaningsInDbForWord(word.getId(), word.getSpelling(), 1);
        Meaning meaning = createdMeaningForWord.get(word.getId()).get(0);

        Assert.assertNotNull(meaning);
        Assert.assertNotNull(meaning.getId());

        Result result = Helpers.route(Helpers.fakeRequest(Helpers.DELETE,"/api/v1/words/" + "invalidWordId" + "/meanings/" + meaning.getId()));
        Assert.assertEquals(Helpers.NOT_FOUND, result.status());
        Assert.assertEquals(Constants.Messages.EntityNotFound( "invalidWordId"), contentAsString(result));
    }

    /* Add Antonym Test */
    @Test
    public void addAntonym_validSpellingOfNonExistingWord_antonymAddedCorrectly() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);

            String jsonAntonymString = "{\n" +
                "  \"spelling\" : \"বিবর্তিত\",\n" +
                "  \"targetWordId\" : null,\n" +
                "  \"strength\" : 0\n" +
                "}";

            JsonNode body = JsonUtil.jStringToJNode(jsonAntonymString);
            Result result = Helpers.route(Helpers.fakeRequest(POST,"/api/v1/words/" + word.getId() + "/antonym/add")
                .bodyJson(body));
            Assert.assertEquals(Helpers.CREATED, result.status());
            JsonNode resultsJson = JsonUtil.jStringToJNode(contentAsString(result));
            Assert.assertEquals(body, resultsJson);
        });
    }

    @Test
    public void addAntonym_validSpellingOfExistingWord_antonymAddedCorrectly() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(2);
            Word word = createdWords.get(0);
            Word targetWord = createdWords.get(1);

            String jsonAntonymString = "{\n" +
                "  \"spelling\" : \"" + targetWord.getSpelling() +"\",\n" +
                "  \"targetWordId\" : \"" + targetWord.getId() + "\",\n" +
                "  \"strength\" : 0\n" +
                "}";

            JsonNode body = JsonUtil.jStringToJNode(jsonAntonymString);
            Result result = Helpers.route(Helpers.fakeRequest(POST,"/api/v1/words/" + word.getId() + "/antonym/add")
                .bodyJson(body));
            Assert.assertEquals(Helpers.CREATED, result.status());
            JsonNode resultsJson = JsonUtil.jStringToJNode(contentAsString(result));
            Assert.assertEquals(body, resultsJson);
        });
    }

    /* Add Synonym Test */
    @Test
    public void addSynonym_validSpellingOfNonExistingWord_synonymAddedCorrectly() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(1);
            Word word = createdWords.get(0);

            String jsonSynonymString = "{\n" +
                "  \"spelling\" : \"বিবর্তিত\",\n" +
                "  \"targetWordId\" : null,\n" +
                "  \"strength\" : 0\n" +
                "}";

            JsonNode body = JsonUtil.jStringToJNode(jsonSynonymString);
            Result result = Helpers.route(Helpers.fakeRequest(POST,"/api/v1/words/" + word.getId() + "/synonym/add")
                .bodyJson(body));
            Assert.assertEquals(Helpers.CREATED, result.status());
            JsonNode resultsJson = JsonUtil.jStringToJNode(contentAsString(result));
            Assert.assertEquals(body, resultsJson);
        });
    }

    @Test
    public void addSynonym_validSpellingOfExistingWord_synonymAddedCorrectly() {

        Helpers.running(Helpers.fakeApplication(), () -> {

            createWordsInDb(2);
            Word word = createdWords.get(0);
            Word targetWord = createdWords.get(1);

            String jsonSynonymString = "{\n" +
                "  \"spelling\" : \"" + targetWord.getSpelling() +"\",\n" +
                "  \"targetWordId\" : \"" + targetWord.getId() + "\",\n" +
                "  \"strength\" : 0\n" +
                "}";

            JsonNode body = JsonUtil.jStringToJNode(jsonSynonymString);
            Result result = Helpers.route(Helpers.fakeRequest(POST,"/api/v1/words/" + word.getId() + "/synonym/add")
                .bodyJson(body));
            Assert.assertEquals(Helpers.CREATED, result.status());
            JsonNode resultsJson = JsonUtil.jStringToJNode(contentAsString(result));
            Assert.assertEquals(body, resultsJson);
        });
    }

    @Test
    public void searchWordsByPrefix() {

        createWordsInDb(50);

        String spelling = createdWords.get(0).getSpelling();
        String prefix = spelling.substring(0,1);

        log.info("Test searchWordsByPrefix, prefix: " + prefix);

        Set<String> spellingsWithPrefixes = createdWords.stream()
            .filter(word -> word.getSpelling().startsWith(prefix))
            .map(word-> word.getSpelling())
            .collect(Collectors.toSet());

        log.info("Spelling with prefixes:" + spellingsWithPrefixes);

        Helpers.running(Helpers.fakeApplication(), () -> {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Constants.KEY_SEARCH_STRING, prefix);
            JsonNode requestBodyJson = JsonUtil.jStringToJNode(jsonObject.toString());
            Result result = Helpers.route(Helpers.fakeRequest(POST,"/api/v1/words/search").bodyJson(requestBodyJson));
            Assert.assertEquals(Helpers.OK, result.status());

            JsonNode resultsJson = JsonUtil.jStringToJNode(contentAsString(result));
            JsonNode expectedResult = JsonUtil.objectToJNode(spellingsWithPrefixes);
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
}
