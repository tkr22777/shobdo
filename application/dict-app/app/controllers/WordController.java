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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static utilities.ControllerUtils.executeEndpoint;
import static utilities.Headers.X_REQUEST_ID;
import static utilities.Headers.X_TRANSACTION_ID;

/**
 * Created by tahsinkabir on 5/28/16.
 */
public class WordController extends Controller {

    private final WordLogic wordLogic = WordLogic.factory();
    private static LogPrint log = new LogPrint(WordController.class);

    public Result index() {
        return ok( "বাংলা অভিধান এ স্বাগতম!");
    }

    //CREATE
    @BodyParser.Of(BodyParser.Json.class)
    public Result createWord() {

        String transactionId = request().getHeader(X_TRANSACTION_ID);
        String requestId = request().getHeader(X_REQUEST_ID);
        Map<String,String> parameters = new HashMap<>();
        JsonNode wordJson = request().body().asJson();
        parameters.put("body", wordJson.toString());

        return executeEndpoint(transactionId, requestId, "createWord", parameters, () ->
            created(wordLogic.createWord(wordJson))
        );
    }

    //READ
    public Result getWordByWordId(String wordId) {

        String transactionId = request().getHeader(X_TRANSACTION_ID);
        String requestId = request().getHeader(X_REQUEST_ID);
        Map<String,String> parameters = new HashMap<>();

        return executeEndpoint(transactionId, requestId, "getWordByWordId", parameters, () ->
            ok(wordLogic.getWordJNodeByWordId(wordId))
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getWordBySpellingPost() {

        String transactionId = request().getHeader(X_TRANSACTION_ID);
        String requestId = request().getHeader(X_REQUEST_ID);
        Map<String,String> parameters = new HashMap<>();
        JsonNode body =  request().body().asJson();
        parameters.put("body", body.toString());

        return executeEndpoint(transactionId, requestId, "getWordBySpellingPost", parameters, () -> {
            if(!body.has(Constants.WORD_SPELLING_KEY))
                throw new IllegalArgumentException("");

            String wordSpelling = body.get(Constants.WORD_SPELLING_KEY).asText();
            return ok(wordLogic.getWordJNodeBySpelling(wordSpelling));
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateWord(String wordId) {

        String transactionId = request().getHeader(X_TRANSACTION_ID);
        String requestId = request().getHeader(X_REQUEST_ID);
        Map<String,String> parameters = new HashMap<>();
        JsonNode body = request().body().asJson();
        parameters.put("body", body.toString());

        return executeEndpoint(transactionId, requestId, "updateWord", parameters, () ->
            ok(wordLogic.updateWordJNode(wordId, body))
        );
    }

    public Result deleteWord(String wordId) {

        String transactionId = request().getHeader(X_TRANSACTION_ID);
        String requestId = request().getHeader(X_REQUEST_ID);
        Map<String,String> parameters = new HashMap<>();
        parameters.put("wordId", wordId);

        return executeEndpoint(transactionId, requestId, "deleteWord", parameters, () -> {
            log.info("Delete word with id:" + wordId);
            wordLogic.deleteWord(wordId);
            return ok();
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result searchWordsBySpelling() {

        String transactionId = request().getHeader(X_TRANSACTION_ID);
        String requestId = request().getHeader(X_REQUEST_ID);
        Map<String,String> parameters = new HashMap<>();
        JsonNode body = request().body().asJson();
        parameters.put("body", body.toString());

        return executeEndpoint(transactionId, requestId, "searchWordsBySpelling", parameters, () -> {

            if(!body.has(Constants.SEARCH_STRING_KEY))
                badRequest();

            String searchString = body.get(Constants.SEARCH_STRING_KEY).asText();;
            Set<String> wordSpellings = new HashSet<>();

            if (searchString.length() > 0)
                wordSpellings = wordLogic.searchWords(searchString);

            return ok(Json.toJson(wordSpellings));
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result listWords(String startWordId, Integer limit) {
        log.info("List words starting from id:" + startWordId + ", limit:" + limit);
        return ok();
    }

    /* Meaning related API */

    @BodyParser.Of(BodyParser.Json.class)
    public Result createMeaning(String wordId) {

        String transactionId = request().getHeader(X_TRANSACTION_ID);
        String requestId = request().getHeader(X_REQUEST_ID);
        Map<String,String> parameters = new HashMap<>();
        JsonNode body = request().body().asJson();
        parameters.put("body", body.toString());

        return executeEndpoint(transactionId, requestId, "createMeaning" , parameters, () -> {
            log.info("Create meaning: " + body + " on word with id:" + wordId);
            return created(wordLogic.createMeaningJNode(wordId, body));
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getMeaning(String wordId, String meaningId) {

        String transactionId = request().getHeader(X_TRANSACTION_ID);
        String requestId = request().getHeader(X_REQUEST_ID);
        Map<String,String> parameters = new HashMap<>();

        return executeEndpoint(transactionId, requestId, "getMeaning" , parameters, () -> {
            log.info("Get meaning with meaningId:" + meaningId  + " of word with id:" + wordId);
            JsonNode meaningJson = wordLogic.getMeaningJsonNodeByMeaningId(wordId, meaningId);
            return meaningJson == null ? notFound(Constants.ENTITY_NOT_FOUND + meaningId): ok(meaningJson);
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateMeaning(String wordId, String meaningId) {

        String transactionId = request().getHeader(X_TRANSACTION_ID);
        String requestId = request().getHeader(X_REQUEST_ID);
        Map<String,String> parameters = new HashMap<>();

        return executeEndpoint(transactionId, requestId, "updateMeaning", parameters, () -> {
            JsonNode meaningJsonNode = request().body().asJson();
            log.info("Update meaning with meaningId: " + meaningId + " with json:" + meaningJsonNode
                    + " on word with id:" + wordId);
            JsonNode updateMeaningJsonNode = wordLogic.updateMeaningJsonNode(wordId, meaningJsonNode);
            return ok(updateMeaningJsonNode);
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result deleteMeaning(String wordId, String meaningId) {

        String transactionId = request().getHeader(X_TRANSACTION_ID);
        String requestId = request().getHeader(X_REQUEST_ID);
        Map<String,String> parameters = new HashMap<>();

        return executeEndpoint(transactionId, requestId, "deleteMeaning", parameters, () -> {
            log.info("Delete meaning: " + meaningId + " on word with id:" + wordId);
            wordLogic.deleteMeaning(wordId, meaningId);
            return ok();
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result listMeanings(String wordId) {

        String transactionId = request().getHeader(X_TRANSACTION_ID);
        String requestId = request().getHeader(X_REQUEST_ID);
        Map<String,String> parameters = new HashMap<>();

        return executeEndpoint(transactionId, requestId, "listMeanings", parameters, () -> {
            log.info("List meaningsMap on word with id:" + wordId);
            wordLogic.listMeanings(wordId);
            return ok();
        });
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
