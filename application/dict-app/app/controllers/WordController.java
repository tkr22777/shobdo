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
import utilities.Constants;
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

    private final WordLogic logic = WordLogic.factory();

    private static LogPrint log = new LogPrint(WordController.class);

    public Result index() {

        String welcome = "বাংলা অভিধান এ স্বাগতম!";

        log.info(welcome);

        return ok(welcome);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result searchWordsBySpelling() {

        String spelling = "";

        JsonNode json = request().body().asJson();

        Set<String> wordSpellings = new HashSet<>();

        try {

            spelling = json.get("spelling").asText();

            if(spelling.length() > 0)
                wordSpellings = logic.searchWordsBySpelling(spelling);

        } catch (Exception ex) {

            log.info("WC001 Property 'spelling' not found in the json body. Body found:" + json.textValue());
            log.info("WC002 Exception Stacktrace:" + ex.getStackTrace().toString());
            return badRequest();
        }

        return ok( Json.toJson( wordSpellings ) );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getWordBySpelling() {

        String spelling;

        JsonNode json = request().body().asJson();
        log.info("Json: " + json.textValue());

        try {

            spelling = json.get("spelling").asText();

        } catch (Exception ex) {

            log.info("WC003 Property 'spelling' not found in the json body. Body found:" + json.textValue());
            log.info("WC004 Exception Stacktrace:" + ex.getStackTrace().toString());
            return badRequest();
        }

        DictionaryWord word = logic.getDictionaryWordBySpelling(spelling);

        if( word == null )
            return ok("No word found for spelling:\"" + spelling + "\"");
        else
            return ok( Json.toJson(word) );

    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getWordByWordId() {

        JsonNode json = request().body().asJson();

        String wordId;

        try {

            wordId = json.get("wordId").asText();

        } catch (Exception ex) {

            log.info("WC005 Property 'wordId' not found in the json body. Body found:" + json.textValue());
            log.info("WC006 Exception Stacktrace:" + ex.getStackTrace().toString());

            return badRequest();
        }

        DictionaryWord word = logic.getDictionaryWordByWordId( wordId );

        if(word == null)
            return ok("No word found for wordId:\"" + wordId + "\"");
        else
            return ok( Json.toJson(word) );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createRandomDictionary() { //remove this route for eventual deployment

        JsonNode json = request().body().asJson();

        int wordCount;

        try {

            wordCount = Integer.parseInt( json.get("wordCount").asText() );

        } catch (Exception ex) {

            log.info("WC007 Property 'wordCount' not found in the json body. Body found:" + json.textValue());
            log.info("WC008 Exception Stacktrace:" + ex.getStackTrace().toString());

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
