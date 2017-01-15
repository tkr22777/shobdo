package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.data.DynamicForm;
import play.libs.Json;
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

    public Result testGet() {

        log.info("In testGet!");

        ObjectNode result = Json.newObject();
        result.put("Application", "Dictionary");
        result.put("Language", "Bengali");

        return ok(result);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result testPost() {

        JsonNode json = request().body().asJson();

        if(json.get("name") == null)
            return badRequest("No field with \"name\" ");

        String name = json.get("name").asText();

        log.info("@AC001 Name:"  + name);

        ObjectNode result = Json.newObject();

        result.put("Name", name);
        result.put("Length", name.length());
        result.put("StartsWith", name.charAt(0) + "");

        return ok(result);
    }
}
