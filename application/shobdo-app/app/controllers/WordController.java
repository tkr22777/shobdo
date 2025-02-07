package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import common.stores.MongoStoreFactory;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Constants;
import utilities.ShobdoLogger;
import word.caches.WordCache;
import word.WordLogic;
import word.stores.WordStoreMongoImpl;
import word.objects.Meaning;
import java.util.HashMap;
import java.util.Map;

public class WordController extends Controller {
    private static WordLogic wordLogic;
    private static final ShobdoLogger logger = new ShobdoLogger(WordController.class);

    public Result index() {
        return ok("বাংলা অভিধান এ স্বাগতম!");
    }

    public WordController() {
        WordStoreMongoImpl storeMongoDB = new WordStoreMongoImpl(MongoStoreFactory.getWordCollection());
        wordLogic = new WordLogic(storeMongoDB, WordCache.getCache());
    }

    //CREATE
    @BodyParser.Of(BodyParser.Json.class)
    public Result createWord() {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode wordJson = request().body().asJson();
        parameters.put("requestBody", wordJson.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "createWord", parameters,
            () -> created(
                wordLogic.createWord(wordJson)
                    .jsonNode()
            )
        );
    }

    //READ
    public Result getWordByWordId(final String wordId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        return ControllerUtils.executeEndpoint(transactionId, requestId, "getWordById", parameters,
            () -> ok(
                wordLogic.getWordById(wordId)
                    .jsonNode()
            )
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getWordBySpellingPost() {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body =  request().body().asJson();
        parameters.put("requestBody", body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "getWordBySpelling", parameters,
            () -> {
                if (!body.has(Constants.KEY_SPELLING)) {
                    throw new IllegalArgumentException("Word spelling has not been provided");
                }

                final String spelling = body.get(Constants.KEY_SPELLING).asText();
                return ok(
                    wordLogic.getWordBySpelling(spelling)
                        .jsonNode()
                );
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateWord(final String wordId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "updateWordWithUserRequest", parameters,
            () -> ok(
                wordLogic.updateWord(wordId, body)
                    .jsonNode()
            )
        );
    }

    public Result deleteWord(final String wordId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);
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

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "searchWordsBySpelling", parameters,
            () -> {
                if (!body.has(Constants.KEY_SEARCH_STRING)) {
                    return badRequest();
                }
                final String searchString = body.get(Constants.KEY_SEARCH_STRING).asText();
                return ok(Json.toJson(wordLogic.searchWords(searchString)));
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

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "createMeaning" , parameters,
            () -> created(
                wordLogic.createMeaning(wordId, body)
                    .jsonNode()
            )
        );
    }

    public Result getMeaning(final String wordId, final String meaningId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "getMeaning" , new HashMap<>(),
            () -> {
                final Meaning meaning = wordLogic.getMeaning(wordId, meaningId);
                return meaning == null ? notFound(Constants.Messages.EntityNotFound(meaningId)) :
                    ok(meaning.jsonNode());
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result updateMeaning(final String wordId, final String meaningId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "updateMeaning", new HashMap<>(),
            () -> {
                final JsonNode meaningJsonNode = request().body().asJson();
                return ok(wordLogic.updateMeaning(wordId, meaningId, meaningJsonNode)
                    .jsonNode()
                );
            }
        );
    }

    public Result deleteMeaning(final String wordId, final String meaningId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "deleteMeaning", new HashMap<>(),
            () -> {
                logger.debug("Delete meaning: " + meaningId + " on word with id:" + wordId);
                wordLogic.deleteMeaning(wordId, meaningId);
                return ok();
            }
        );
    }

    public Result listMeanings(final String wordId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "listMeanings", new HashMap<>(),
            () -> {
                logger.debug("List meanings on word with id:" + wordId);
                wordLogic.listMeanings(wordId);
                return ok();
            }
        );
    }

    /* Antonym related API */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addAntonym(final String wordId, final String meaningId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "addAntonym", parameters,
            () -> {
                if (!body.has(Constants.KEY_ANTONYM)) {
                    return badRequest();
                }

                final String antonym = body.get(Constants.KEY_ANTONYM).asText();
                wordLogic.addAntonym(wordId,meaningId, antonym);

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Constants.KEY_ANTONYM, antonym);
                return created(jsonObject.toString());
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result removeAntonym(final String wordId, final String meaningId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "removeAntonym", parameters,
            () -> {
                if (!body.has(Constants.KEY_ANTONYM)) {
                    return badRequest();
                }

                final String antonym = body.get(Constants.KEY_ANTONYM).asText();
                wordLogic.removeAntonym(wordId, meaningId, antonym);
                return ok();
            }
        );
    }

    /* Synonym related API */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addSynonym(final String wordId, final String meaningId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());


        return ControllerUtils.executeEndpoint(transactionId, requestId, "addSynonym", parameters,
            () -> {
                if (!body.has(Constants.KEY_SYNONYM)) {
                    return badRequest();
                }

                final String synonym = body.get(Constants.KEY_SYNONYM).asText();
                wordLogic.addSynonym(wordId, meaningId, synonym);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Constants.KEY_SYNONYM, synonym);
                return created(jsonObject.toString());
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result removeSynonym(final String wordId, final String meaningId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "removeSynonym", parameters,
            () -> {
                if (!body.has(Constants.KEY_SYNONYM)) {
                    return badRequest();
                }

                final String synonym = body.get(Constants.KEY_SYNONYM).asText();
                wordLogic.removeSynonym(wordId, meaningId, synonym);
                return ok();
            }
        );
    }
}
