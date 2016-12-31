package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import scala.Int;
import utilities.LogPrint;

/**
 * Created by tahsinkabir on 12/31/16.
 */
public class AdminController extends Controller {

    private static LogPrint log = new LogPrint(AdminController.class);

    public Result testLength(String word, int length) {

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
    public Result testPost() {

        JsonNode json = request().body().asJson();

        return ok("Got name: " + json.get("name").asText());
    }

}
