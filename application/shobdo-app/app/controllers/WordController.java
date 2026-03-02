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
import word.objects.Word;
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

    //READ word of the day — same word for all users on a given UTC date
    public Result getWordOfDay() {
        return ControllerUtils.executeEndpoint("", "", "getWordOfDay", new HashMap<>(),
            () -> ok(wordLogic.getWordOfDay().jsonNode())
        );
    }

    //READ random
    public Result getRandomWord() {
        return ControllerUtils.executeEndpoint("", "", "getRandomWord", new HashMap<>(),
            () -> ok(wordLogic.getRandomWord().jsonNode())
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

    // OG preview page for social/link-preview bots: GET /bn/word/<spelling>
    // Returns minimal HTML with word-specific og:title / og:description baked in.
    // Real browsers are immediately bounced back to the SPA via window.location.replace.
    public Result wordOgPage(final String spelling) {
        return ControllerUtils.executeEndpoint("", "", "wordOgPage", new HashMap<>(),
            () -> {
                final Word word = wordLogic.getWordBySpelling(spelling);
                final String firstMeaningText = (word.getMeanings() != null && !word.getMeanings().isEmpty())
                    ? word.getMeanings().values().iterator().next().getText()
                    : "";
                final String desc = spelling + " এর অর্থ: " + firstMeaningText;
                final String truncDesc = desc.length() > 160 ? desc.substring(0, 160) : desc;
                final String encodedSpelling;
                try {
                    encodedSpelling = java.net.URLEncoder.encode(spelling, "UTF-8").replace("+", "%20");
                } catch (java.io.UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                final String canonicalUrl = "https://www.shobdo.info/bn/word/" + encodedSpelling;
                final String title = spelling + " - অর্থ ও সংজ্ঞা | শব্দ";
                final String html =
                    "<!DOCTYPE html>\n<html lang=\"bn\">\n<head>\n" +
                    "<meta charset=\"UTF-8\">\n" +
                    "<title>" + escapeHtml(title) + "</title>\n" +
                    "<meta property=\"og:title\" content=\"" + escapeHtml(title) + "\" />\n" +
                    "<meta property=\"og:description\" content=\"" + escapeHtml(truncDesc) + "\" />\n" +
                    "<meta property=\"og:url\" content=\"" + canonicalUrl + "\" />\n" +
                    "<meta property=\"og:type\" content=\"website\" />\n" +
                    "<meta name=\"twitter:card\" content=\"summary\" />\n" +
                    "<meta name=\"twitter:title\" content=\"" + escapeHtml(title) + "\" />\n" +
                    "<meta name=\"twitter:description\" content=\"" + escapeHtml(truncDesc) + "\" />\n" +
                    "<link rel=\"canonical\" href=\"" + canonicalUrl + "\" />\n" +
                    "<script>window.location.replace(\"" + canonicalUrl + "\");</script>\n" +
                    "</head>\n<body>\n" +
                    "<h1>" + escapeHtml(spelling) + "</h1>\n" +
                    "<p>" + escapeHtml(truncDesc) + "</p>\n" +
                    "</body>\n</html>\n";
                return ok(html).as("text/html; charset=utf-8");
            }
        );
    }

    private static String escapeHtml(final String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    //READ by lang + spelling: GET /api/v1/bn/word/<spelling>
    public Result getWordByLangAndSpelling(final String lang, final String spelling) {
        return ControllerUtils.executeEndpoint("", "", "getWordByLangAndSpelling", new HashMap<>(),
            () -> {
                if (!"bn".equals(lang)) {
                    return badRequest("Unsupported language: " + lang);
                }
                return ok(wordLogic.getWordBySpelling(spelling).jsonNode());
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
                return ok(Json.toJson(
                    wordLogic.searchWords(searchString).stream()
                        .map(w -> { java.util.Map<String,String> m = new java.util.LinkedHashMap<>(); m.put("id", w.getId()); m.put("spelling", w.getSpelling()); return m; })
                        .collect(java.util.stream.Collectors.toList())
                ));
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
