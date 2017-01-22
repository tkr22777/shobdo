package controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import logics.WordLogic;
import objects.DictionaryWord;
import play.data.DynamicForm;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import scala.Int;
import utilities.DictUtil;
import utilities.JsonUtil;
import utilities.LogPrint;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tahsinkabir on 5/28/16.
 */
public class WordController extends Controller{

    @Inject WSClient wsClient;

    private final WordLogic logic = WordLogic.factory(null);

    private static LogPrint log = new LogPrint(WordController.class);

    public Result index() {

        String welcome = "বাংলা অভিধান এ স্বাগতম!";

        log.info(welcome);

        return ok(welcome);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result searchWordsBySpelling() {

        //For some reason the limit value is not equal to the size of array that is returned
        int limit = Integer.MAX_VALUE;

        JsonNode json = request().body().asJson();
        String spelling;
        Set<String> wordSpellings = new HashSet<>();

        try {

            spelling = json.get("spelling").asText();
            wordSpellings = logic.searchWordsBySpelling( spelling, limit);

        } catch (Exception ex) {

            log.info("WC002 Property 'spelling' not found in the json body. Body found:" + json.textValue());
            log.info("WC003 Exception Stacktrace:" + ex.getStackTrace());
            return badRequest();
        }

        return ok( Json.toJson( wordSpellings ) );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getWordBySpelling() {

        JsonNode json = request().body().asJson();
        String spelling = "null";

        try {

            spelling = json.get("spelling").asText();

        } catch (Exception ex) {

            log.info("WC001 getWordBySpelling [Spelling:" + spelling  + "]  Exception: " + ex.getMessage());
            return badRequest();
        }

        return ok( Json.toJson(logic.getDictionaryWordBySpelling(spelling)) );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getWordByWordId() {

        JsonNode json = request().body().asJson();

        String wordId;

        try {

            wordId = json.get("wordId").asText();

        } catch (Exception ex) {

            log.info("Exception: " + ex.getMessage());

            return badRequest();
        }

        return ok( Json.toJson(logic.getDictionaryWordByWordId( wordId )) );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createRandomDictionary() {

        JsonNode json = request().body().asJson();

        int wordCount;

        try {

            wordCount = Integer.parseInt( json.get("wordCount").asText() );

        } catch (Exception ex) {

            log.info("Exception: " + ex.getMessage());

            return badRequest();
        }

        Set<DictionaryWord> words = DictUtil.generateDictionaryWithRandomWords( wordCount );

        for(DictionaryWord word:words)
            logic.saveDictionaryWord(word);

        return ok("Generated and added " + wordCount + " random words on the dictionary!");
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createWord() {

        JsonNode json = request().body().asJson();

        DictionaryWord word = (DictionaryWord) JsonUtil.jsonNodeToObject(json, DictionaryWord.class);

        log.info(word.toString());

        return ok();
    }
}
