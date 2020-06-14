package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import logics.WordLogic;
import objects.Meaning;
import objects.Word;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import objects.Constants;
import utilities.DictUtil;
import utilities.ShobdoLogger;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);
        final Map<String,String> parameters = new HashMap<>();

        final JsonNode body = request().body().asJson();
        parameters.put("requestBody", body.toString());

        return ControllerUtils.executeEndpoint(transactionId, requestId, "createMeaning" , parameters,
            () -> created(
                wordLogic.createMeaning(wordId, body)
                    .toAPIJNode()
            )
        );
    }

    public Result getMeaning(final String wordId, final String meaningId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "getMeaning" , new HashMap<>(),
            () -> {
                final Meaning meaning = wordLogic.getMeaning(wordId, meaningId);
                return meaning == null ? notFound(Constants.Messages.EntityNotFound(meaningId)) :
                    ok(meaning.toAPIJNode());
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
                return ok(wordLogic.updateMeaning(wordId, meaningId, meaningJsonNode)
                    .toAPIJNode()
                );
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

    /* Antonym related API */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addAntonym(final String wordId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "addAntonym", new HashMap<>(),
            () -> {
                return created(
                    wordLogic.addAntonym(wordId, request().body().asJson())
                        .toAPIJsonNode()
                );
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result removeAntonym(final String wordId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "removeAntonym", new HashMap<>(),
            () -> {
                wordLogic.removeAntonym(wordId, request().body().asJson());
                return ok();
            }
        );
    }

    /* Synonym related API */
    @BodyParser.Of(BodyParser.Json.class)
    public Result addSynonym(final String wordId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "addSynonym", new HashMap<>(),
            () -> {
                return created(
                    wordLogic.addSynonym(wordId, request().body().asJson())
                        .toAPIJsonNode()
                );
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result removeSynonym(final String wordId) {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "removeSynonym", new HashMap<>(),
            () -> {
                wordLogic.removeSynonym(wordId, request().body().asJson());
                return ok();
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createRandomDictionary() { //remove this route for eventual deployment

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "listMeanings", new HashMap<>(),
            () -> {

                Set<String> wordSpellingSet = new HashSet<>();
                Set<Word> words = new HashSet<>();

                final int wordCount = Integer.parseInt(request().body().asJson().get(Constants.WORD_COUNT_KEY).asText());
                logger.info("Total word creation requested:" + wordCount);

                for (int count = 0; count < wordCount; count++) {
                    int numOfTries = 100;
                    for (int tryCount = 0; tryCount < numOfTries; tryCount++) {
                        Word word = DictUtil.genARandomWord();
                        if (wordSpellingSet.contains(word.getSpelling())) {
                            continue;
                        }
                        wordSpellingSet.add(word.getSpelling());
                        words.add(word);
                        break;
                    }
                }

                logger.info("Total words for be created:" + words.size());

                List<Word> createdWords = words.stream()
                    .map( w -> {
                        try {
                            return wordLogic.createWord(w);
                        } catch (Exception ex) {
                            return null;
                        }
                    }
                    )
                    .filter(w -> w != null)
                    .collect(Collectors.toList());

                logger.info("Total words created:" + createdWords.size());

                List<Meaning> allMeanings = new ArrayList<>();

                for (Word word: createdWords) {
                    allMeanings.addAll(
                        DictUtil.genMeaning(word.getSpelling(), DictUtil.randIntInRange(1, 5))
                            .stream()
                            .map(meaning -> wordLogic.createMeaning(word.getId(), meaning))
                            .collect(Collectors.toList())
                    );
                }

                logger.info("Total meanings created:" + allMeanings.size());

                return ok(String.format("Generated and added %s random words, %s meanings on the dictionary!",
                    createdWords.size(), allMeanings.size()));
            }
        );

    }
}
