package controllers;

import com.fasterxml.jackson.databind.JsonNode;
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
import utilities.LogPrint;

import javax.inject.Inject;
import java.util.Set;

/**
 * Created by tahsinkabir on 5/28/16.
 */
public class WordController extends Controller{

    @Inject WSClient wsClient;

    WordLogic logic = WordLogic.factory(null);

    private static LogPrint log = new LogPrint(WordController.class);

    public Result index(){
        return ok("বাংলা অভিধান এ স্বাগতম!" );
    }

    public Result testLength(String word, int length){

        int s_size = word.length();

        int len = Int.unbox(length);

        if(s_size == len)
            return ok("Yay! You made the correct character count of the word: " + word );
        if(s_size > len)
            return ok("Ups, the length of " + word + " is bigger than you thought!" );
        else
            return ok("Ups, the length of " + word + " is smaller than you thought!" );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result searchWordsBySpelling(){

        //For some reason the limit value is not equal to the size of array that is returned
        int limit = Integer.MAX_VALUE;

        JsonNode json = request().body().asJson();

        String spelling;

        try {

            spelling = json.get("spelling").asText();

        } catch (Exception ex) {

            log.info("Property 'spelling' not found in the json body. Body found:" + json.textValue());
            return badRequest();
        }

        return ok( Json.toJson( logic.searchWordsBySpelling( spelling, limit).toString() ) );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getWordBySpelling(){

        JsonNode json = request().body().asJson();
        String spelling;

        try {

            spelling = json.get("spelling").asText();

        } catch (Exception ex) {

            log.info("Exception: " + ex.getMessage());

            return badRequest();
        }

        return ok( Json.toJson(logic.getDictionaryWordBySpelling(spelling)) );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result getWordByWordId(){

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

        return ok();
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result testPost() {

        JsonNode json = request().body().asJson();

        return ok("Got name: " + json.get("name").asText());

    }

}
