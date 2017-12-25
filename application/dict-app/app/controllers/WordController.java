package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import logics.WordLogic;
import objects.Word;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Constants;
import utilities.DictUtil;
import utilities.LogPrint;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tahsinkabir on 5/28/16.
 */
public class WordController extends Controller {

    private final WordLogic wordLogic = WordLogic.factory();
    private static LogPrint log = new LogPrint(WordController.class);

    public Result index() {

        String welcome = "বাংলা অভিধান এ স্বাগতম!";
        return ok(welcome);
    }

    //CREATE
    @BodyParser.Of(BodyParser.Json.class)
    public Result createWord() {

        JsonNode wordJson = request().body().asJson();

        try {

            return created(wordLogic.createWord(wordJson));

        } catch (Exception ex) {

            return ex instanceof IllegalArgumentException? badRequest(ex.getMessage()): internalServerError(ex.getMessage());
        }
    }

    //READ
    public Result getWordByWordId(String wordId) {

        try {

            log.info("001 getWordById, id:" + wordId);
            JsonNode wordJson = wordLogic.getWordJNodeByWordId(wordId);
            return wordJson == null? notFound(Constants.ENTITY_NOT_FOUND + wordId): ok(wordJson);

        } catch (Exception ex) {

            return ex instanceof IllegalArgumentException ? badRequest(ex.getMessage()) : internalServerError(ex.getMessage());
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getWordBySpellingPost() {

        try {

            JsonNode body =  request().body().asJson();

            if(!body.has(Constants.WORD_SPELLING_KEY))
                throw new IllegalArgumentException("");

            String wordSpelling = body.get(Constants.WORD_SPELLING_KEY).asText();
            JsonNode wordNode = wordLogic.getWordJNodeBySpelling(wordSpelling);
            return wordNode == null? notFound(Constants.ENTITY_NOT_FOUND + wordSpelling): ok(wordNode);

        } catch (Exception ex) {

            return ex instanceof IllegalArgumentException ? badRequest(ex.getMessage()) : internalServerError(ex.getMessage());
        }
    }

    //UPDATE TODO
    @BodyParser.Of(BodyParser.Json.class) public Result updateWord(String wordId) {

        JsonNode wordJson = request().body().asJson();

        try {

            return ok(wordLogic.updateWordJNode(wordId, wordJson));

        } catch (Exception ex) {

            return ex instanceof IllegalArgumentException? badRequest(ex.getMessage()): internalServerError(ex.getMessage());
        }
    }

    //DELETE TODO
    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteWord(String wordId) {

        log.info("Delete word with id:" + wordId);

        wordLogic.deleteWord(wordId);

        return ok();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result searchWordsBySpelling() {

        String searchString = "";
        JsonNode json = request().body().asJson();
        Set<String> wordSpellings = new HashSet<>();

        try {

            searchString = json.get(Constants.SEARCH_STRING_KEY).asText();

            if (searchString.length() > 0)
                wordSpellings = wordLogic.searchWords(searchString);

        } catch (Exception ex) {

            log.info("WC001 Property 'searchString' not found in the json body. Body found:" + json.textValue());
            log.info("WC002 Exception Stacktrace:" + ex.getStackTrace().toString());
            return badRequest();
        }

        return ok(Json.toJson(wordSpellings));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result listWords(String startWordId, Integer limit) {
        log.info("List words starting from id:" + startWordId + ", limit:" + limit);
        return ok();
    }

    /* Meaning related API */

    @BodyParser.Of(BodyParser.Json.class)
    public Result createMeaning(String wordId) {
        JsonNode json = request().body().asJson();
        log.info("Create meaning: " + json + " on word with id:" + wordId);
        wordLogic.createMeaning(wordId, null);
        return ok();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getMeaning(String wordId, String meaningId) {
        log.info("Get meaning with meaningId:" + meaningId  + " of word with id:" + wordId);
        wordLogic.getMeaning(wordId, meaningId);
        return ok();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateMeaning(String wordId, String meaningId) {
        JsonNode json = request().body().asJson();
        log.info("Update meaning with meaningId: " + meaningId + " with json:" + json
                + " on word with id:" + wordId);
        wordLogic.updateMeaning(wordId, null);
        return ok();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteMeaning(String wordId, String meaningId) {
        log.info("Delete meaning: " + meaningId + " on word with id:" + wordId);
        wordLogic.deleteMeaning(wordId, meaningId);
        return ok();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result listMeanings(String wordId) {
        log.info("List meaningsMap on word with id:" + wordId);
        wordLogic.listMeanings(wordId);
        return ok();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createRandomDictionary() { //remove this route for eventual deployment

        JsonNode json = request().body().asJson();

        int wordCount;

        try {

            wordCount = Integer.parseInt(json.get(Constants.WORD_COUNT_KEY).asText());

        } catch (Exception ex) {

            log.info("WC007 Property 'wordCount' not found in the json body. Body found:" + json.textValue());
            log.info("WC008 Exception Stacktrace:" + ex.getStackTrace().toString());

            return badRequest();
        }

        Set<Word> words = DictUtil.generateRandomWordSet(wordCount);

        for (Word word : words)
            wordLogic.createWord(word);

        return ok("Generated and added " + wordCount + " random words on the dictionary!");
    }

}
