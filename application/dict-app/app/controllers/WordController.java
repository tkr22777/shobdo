package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import logics.WordLogic;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import objects.Constants;
import utilities.DictUtil;
import utilities.LogPrint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static utilities.ControllerUtils.executeEndpoint;
import static controllers.Headers.X_REQUEST_ID;
import static controllers.Headers.X_TRANSACTION_ID;

/**
 * Created by Tahsin Kabir on 5/28/16.
 */
public class WordController extends Controller {

    private static final WordLogic wordLogic = WordLogic.createMongoBackedWordLogic();
    private static final LogPrint logger = new LogPrint(WordController.class);

    public Result index() {
        return ok("বাংলা অভিধান এ স্বাগতম!");
    }

    //CREATE
    @BodyParser.Of(BodyParser.Json.class)
    public Result createWord() {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode wordJson = request().body().asJson();
        parameters.put("requestBody", wordJson.toString());

        return executeEndpoint(transactionId, requestId, "createWord", parameters, () ->
            created(wordLogic.createWord(wordJson).toJson())
        );
    }

    //READ
    public Result getWordByWordId(final String wordId) {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        return executeEndpoint(transactionId, requestId, "getWordById", parameters, () ->
            ok(wordLogic.getWordById(wordId).toJson())
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getWordBySpellingPost() throws IOException {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body =  request().body().asJson();
        parameters.put("requestBody", body.toString());

        return executeEndpoint(transactionId, requestId, "getWordBySpelling", parameters, () -> {
            if (!body.has(Constants.WORD_SPELLING_KEY)) {
                throw new IllegalArgumentException("Word spelling has not been provided");
            }

            final String wordSpelling = body.get(Constants.WORD_SPELLING_KEY).asText();
            try {
                return ok(wordLogic.getWordBySpelling(wordSpelling).toJson());
            } catch (Exception ex) {
                throw new RuntimeException("Server error while getting word by spelling");
            }
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateWord(final String wordId) {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return executeEndpoint(transactionId, requestId, "updateWordVersioned", parameters, () ->
            ok(wordLogic.updateWord(wordId, body).toJson())
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateWordUserRequest(final String wordId) {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return executeEndpoint(transactionId, requestId, "updateWordVersioned", parameters, () ->
                ok(wordLogic.updateWordVersioned(wordId, body).toJson())
        );
    }

    public Result deleteWord(final String wordId) {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        parameters.put("wordId", wordId);

        return executeEndpoint(transactionId, requestId, "deleteWord", parameters, () -> {
            wordLogic.deleteWord(wordId);
            return ok();
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result searchWordsBySpelling() {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return executeEndpoint(transactionId, requestId, "searchWordsBySpelling", parameters, () -> {
            if (!body.has(Constants.SEARCH_STRING_KEY)) {
                return badRequest();
            }
            final String searchString = body.get(Constants.SEARCH_STRING_KEY).asText();
            if  (searchString.length() > 0) {
                return ok(Json.toJson(wordLogic.searchWords(searchString)));
            } else {
                return ok(Json.toJson(new HashMap<>()));
            }
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result listWords(final String startWordId, final Integer limit) {
        logger.info("List words beginning wordId:" + startWordId + ", limit:" + limit);
        return ok();
    }

    /* Meaning related API */
    @BodyParser.Of(BodyParser.Json.class)
    public Result createMeaning(final String wordId) {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("wordId", wordId);
        parameters.put("requestBody", body.toString());

        return executeEndpoint(transactionId, requestId, "createMeaning" , parameters, () ->
            created(wordLogic.createMeaningJNode(wordId, body))
        );
    }

    public Result getMeaning(final String wordId, final String meaningId) {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);

        return executeEndpoint(transactionId, requestId, "getMeaning" , new HashMap<>(), () -> {
            logger.info("Get meaning with meaningId:" + meaningId  + " of word with id:" + wordId);
            final JsonNode meaningJson = wordLogic.getMeaningJsonNodeByMeaningId(wordId, meaningId);
            return meaningJson == null ? notFound(Constants.ENTITY_NOT_FOUND + meaningId): ok(meaningJson);
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateMeaning(final String wordId, final String meaningId) {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);

        return executeEndpoint(transactionId, requestId, "updateMeaning", new HashMap<>(), () -> {
            final JsonNode meaningJsonNode = request().body().asJson();
            logger.info("Update meaning with meaningId: " + meaningId + " with json:" + meaningJsonNode
                    + " on word with id:" + wordId);
            final JsonNode updateMeaningJsonNode = wordLogic.updateMeaningJsonNode(wordId, meaningId, meaningJsonNode);
            return ok(updateMeaningJsonNode);
        });
    }

    public Result deleteMeaning(final String wordId, final String meaningId) {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);

        return executeEndpoint(transactionId, requestId, "deleteMeaning", new HashMap<>(), () -> {
            logger.debug("Delete meaning: " + meaningId + " on word with id:" + wordId);
            wordLogic.deleteMeaning(wordId, meaningId);
            return ok();
        });
    }

    public Result listMeanings(final String wordId) {

        final String transactionId = request().getHeader(X_TRANSACTION_ID);
        final String requestId = request().getHeader(X_REQUEST_ID);

        return executeEndpoint(transactionId, requestId, "listMeanings", new HashMap<>(), () -> {
            logger.debug("List meaningsMap on word with id:" + wordId);
            wordLogic.listMeanings(wordId);
            return ok();
        });
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createRandomDictionary() { //remove this route for eventual deployment

        final JsonNode json = request().body().asJson();
        final int wordCount;

        try {
            wordCount = Integer.parseInt(json.get(Constants.WORD_COUNT_KEY).asText());
        } catch (Exception ex) {
            logger.info("WC007 Property 'wordCount' not found in the json requestBody. Body found:" + json.textValue());
            logger.info("WC008 Exception Stacktrace:" + ex.getStackTrace().toString());
            return badRequest();
        }

        DictUtil.generateRandomWordSet(wordCount).forEach(w->wordLogic.createWord(w));
        return ok("Generated and added " + wordCount + " random words on the dictionary!");
    }
}
