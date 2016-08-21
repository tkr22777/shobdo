import java.awt.image.LookupOp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.*;

import play.Logger;
import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;
import play.twirl.api.Content;
import scala.App;
import utilities.Bangla;
import utilities.LogPrint;

import static play.test.Helpers.*;
import static org.junit.Assert.*;


/**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ApplicationTest {

    LogPrint log;

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test
    public void renderTemplate() {
        //Content html = views.html.ndex.render("Your new application is ready.");
        //assertEquals("text/html", html.contentType());
        //assertTrue(html.body().contains("Your new application is ready."));
    }

    @Before
    public void setup() {

        log = new LogPrint(ApplicationTest.class);

    }

    @Test
    public void testBangla() {

        String start = "995"; //ক
        String end = "9A8"; //ন

        log.info("START!");

        int WORDS_TO_GENERATE = 5;

        for(int i = 0 ; i < WORDS_TO_GENERATE ; i++){

            int number = Bangla.randomInRange( 2 , 9);
            String word = Bangla.getWord(start, end, number);
            log.info("Word " + i + " : " + word);
        }

        int SENTENCES_TO_GENERATE = 5;

        for(int i = 0 ; i < SENTENCES_TO_GENERATE ; i++ ){
            int number = Bangla.randomInRange( 4 , 12);
            String sentence = Bangla.getSentence(start, end, number, 12);
            log.info("Sentence " + i + " : " + sentence);
        }

    }

}
