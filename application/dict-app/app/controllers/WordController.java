package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import logics.WordLogic;
import objects.Meaning;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import objects.Constants;
import utilities.DictUtil;
import utilities.ShobdoLogger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tahsin Kabir on 5/28/16.
 */
public class WordController extends Controller {

    private static final WordLogic wordLogic = WordLogic.createMongoBackedWordLogic();
    private static final ShobdoLogger logger = new ShobdoLogger(WordController.class);

    public Result index() {
        return ok("বাংলা অভিধান এ স্বাগতম!");
    }

    //CREATE
    @BodyParser.Of(BodyParser.Json.class)
    public Result createWord() {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode wordJson = request().body().asJson();
        parameters.put("requestBody", wordJson.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "createWord", parameters,
            () -> created(
                wordLogic.createWord(wordJson)
                    .toAPIJsonNode()
            )
        );
    }

    //READ
    public Result getWordByWordId(final String wordId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        return ControllerUtils.executeEndpoint(transactionId, requestId, "getWordById", parameters,
            () -> ok(
                wordLogic.getWordById(wordId)
                    .toAPIJsonNode()
            )
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getWordBySpellingPost() throws IOException {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body =  request().body().asJson();
        parameters.put("requestBody", body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "getWordBySpelling", parameters,
            () -> {
                if (!body.has(Constants.SPELLING_KEY)) {
                    throw new IllegalArgumentException("Word spelling has not been provided");
                }

                final String spelling = body.get(Constants.SPELLING_KEY).asText();
                return ok(
                    wordLogic.getWordBySpelling(spelling)
                        .toAPIJsonNode()
                );
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateWord(final String wordId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "updateWordWithUserRequest", parameters,
            () -> ok(
                wordLogic.updateWord(wordId, body)
                    .toAPIJsonNode()
            )
        );
    }

    public Result deleteWord(final String wordId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        parameters.put("wordId", wordId);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "deleteWord", parameters,
            () -> {
                wordLogic.deleteWord(wordId);
                return ok();
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result searchWordsBySpelling() {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "searchWordsBySpelling", parameters,
            () -> {
                if (!body.has(Constants.SEARCH_STRING_KEY)) {
                    return badRequest();
                }
                final String searchString = body.get(Constants.SEARCH_STRING_KEY).asText();
                if  (searchString.length() > 0) {
                    return ok(Json.toJson(wordLogic.searchWords(searchString)));
                } else {
                    return ok(Json.toJson(new HashMap<>()));
                }
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result listWords(final String startWordId, final Integer limit) {
        logger.info("List words beginning wordId:" + startWordId + ", limit:" + limit);
        return ok();
    }

    /* Meaning related API */
    @BodyParser.Of(BodyParser.Json.class)
    public Result createMeaning(final String wordId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("wordId", wordId);
        parameters.put("requestBody", body.toString());

        logger.info("Here body:" + body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "createMeaning" , parameters,
            () -> created(
                wordLogic.createMeaning(wordId, body)
                    .toAPIJsonNode()
            )
        );
    }

    public Result getMeaning(final String wordId, final String meaningId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "getMeaning" , new HashMap<>(),
            () -> {
                logger.info("Get meaning with meaningId:" + meaningId  + " of word with id:" + wordId);
                final Meaning meaning = wordLogic.getMeaning(wordId, meaningId);
                return meaning == null ? notFound(Constants.Messages.EntityNotFound(meaningId)) :
                    ok(meaning.toAPIJsonNode());
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateMeaning(final String wordId, final String meaningId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "updateMeaning", new HashMap<>(),
            () -> {
                final JsonNode meaningJsonNode = request().body().asJson();
                logger.info("Update meaning with meaningId: " + meaningId + " with json:" + meaningJsonNode
                        + " on word with id:" + wordId);
                return ok(wordLogic.updateMeaning(wordId, meaningId, meaningJsonNode).toAPIJsonNode());
            }
        );
    }

    public Result deleteMeaning(final String wordId, final String meaningId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "deleteMeaning", new HashMap<>(),
            () -> {
                logger.debug("Delete meaning: " + meaningId + " on word with id:" + wordId);
                wordLogic.deleteMeaning(wordId, meaningId);
                return ok();
            }
        );
    }

    public Result listMeanings(final String wordId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "listMeanings", new HashMap<>(),
            () -> {
                logger.debug("List meanings on word with id:" + wordId);
                wordLogic.listMeanings(wordId);
                return ok();
            }
        );
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
