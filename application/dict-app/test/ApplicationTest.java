import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import logics.WordLogic;
import objects.DictionaryWord;
import objects.PartsOfSpeechSet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.BodyParser;
import play.mvc.Http.*;
import play.mvc.Result;
import play.test.WithServer;
import utilities.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static play.test.Helpers.*;

/**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
public class ApplicationTest extends WithServer {

    LogPrint log;

    int numberOfWords = 10;

    Set<DictionaryWord> dictionary = new HashSet<>();

    WordLogic wordLogic;

    @Before
    public void setup() {

        log = new LogPrint(ApplicationTest.class);

        dictionary = DictUtil.generateDictionaryWithRandomWords(numberOfWords);

        wordLogic = WordLogic.factory();
    }

    @Test @Ignore
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test @Ignore
    public void renderTemplate() {
        //Content html = views.html.ndex.render("Your new application is ready.");
        //assertEquals("text/html", html.contentType());
        //assertTrue(html.body().contains("Your new application is ready."));
    }

    @Test
    public void testBangla() {

        int i = 0;
        for(DictionaryWord word: dictionary){
            log.info(" Word "+ i + " :" + word.toString());
            i++;
        }
    }

    @Test @Ignore
    public void storeWordTest() {

        DictionaryWord word = DictUtil.generateARandomWord(new PartsOfSpeechSet());
        wordLogic.saveDictionaryWord(word);
    }

    @Test @Ignore
    public void storeWords() {

        Set<DictionaryWord> words = DictUtil.generateDictionaryWithRandomWords(1217);
        for(DictionaryWord word:words)
            wordLogic.saveDictionaryWord(word);
    }

    @Test @Ignore
    public void totalWords() {
        log.info("Total Words In Dictionary:" + wordLogic.totalWordCount());
    }

    @Test @Ignore
    public void searchWordsByPrefix() throws Exception{

        long current_time = System.nanoTime();

        String prefix = "ত";

        Set<String> results = wordLogic.searchWordsBySpelling(prefix, 10);

        long total_time = System.nanoTime() - current_time;

        log.info("Words for prefix: \"" + prefix + "\":" + results.toString());
        log.info("[Total Time:" + (total_time / 1000000.0) + "ms]");
    }

    @Test @Ignore
    public void searchWordsByPrefixPerformanceTune() throws Exception{

        int i = 0;

        while (i < 10) {

            long current_time = System.nanoTime();

            String prefix = "ত";

            Set<String> results = null;// play.api.cache.Cache.get(prefix, );

            if(results != null) {
                log.info("Found in memory");
            } else {
                log.info("Not found in memory");
                results = wordLogic.searchWordsBySpelling(prefix, 10);
                //if(i == 4)
                    //play.api.cache.Cache.set(prefix, results, 20000, );
            }

            long total_time = System.nanoTime() - current_time;

            log.info("Words for prefix: \"" + prefix + "\":" + results.toString());
            log.info("[Total Time:" + (total_time / 1000000.0) + "ms]");
            i++;
        }
    }

    @Test @Ignore
    public void getWordBySpelling() {

        long current_time = System.nanoTime();

        String wordSpelling = "পিটটান";

        DictionaryWord word = wordLogic.getDictionaryWordBySpelling(wordSpelling);

        long total_time = System.nanoTime() - current_time;

        if (word != null) {
            log.info("Word for spelling: \"" + wordSpelling + "\" :" + word.toString());
        } else {
            log.info("Word for spelling: \"" + wordSpelling + "\":" + "Not Found" );
        }

        log.info("[Total Time:" + (total_time / 1000000.0) + "ms]");
    }

    @Test @Ignore
    public void createDictionaryFromSamsad() throws Exception {

        Collection<DictionaryWord> words = new SamsadExporter().getDictiionary();

        int total = 0;
        for(DictionaryWord word: words) {

            /*
            if(total == 0) break;

            if( "YES".equalsIgnoreCase(word.retrieveExtraMetaValueForKey("SIMPLE_SPELLING"))
             && "YES".equalsIgnoreCase(word.retrieveExtraMetaValueForKey("SIMPLE_MEANING"))
             && "YES".equalsIgnoreCase(word.retrieveExtraMetaValueForKey("UNDERSTANDABLE_TYPE")) ) {

                log.info("Next word: \n" + word.toString());

                wordLogic.saveDictionaryWord(word);
                total++;
            }
            */
            total++;
        }

        log.info("Total words: " + total);
    }

    @Test @Ignore
    public void testConfig() throws Exception {

    }

    @Test @Ignore //Didnt work
    public void createRandomDictionary_RoutePOSTTest() {

        running( fakeApplication(), () -> {

                Map<String, String> body = new HashMap<>();
                body.put("keyT", "valueT");

                RequestBuilder request = new RequestBuilder()
                        .method(POST)
                        .bodyForm(body)
                        .uri("/dict/generate");

                Result result = route(request);
                assertEquals(OK, result.status());
        });
    }

    @Test
    public void rootRouteTest() {

        running( fakeApplication(), () -> {

                Result result = route(fakeRequest(GET, "/"));
                assertEquals(OK, result.status());
                assertEquals("The Bangla Dictionary!",contentAsString(result));
            }
        );
    }

    @Test
    public void getRequestTest() {

        running( fakeApplication(), () -> {

            RequestBuilder request = fakeRequest(GET, "/gettest");
            Result result = route(request);

            assertEquals(OK, result.status());

            JsonNode jsonNode = JsonUtil.toJsonNodeFromJsonString(contentAsString(result));

            assertEquals("Dictionary", jsonNode.get("Application").asText());
            assertEquals("Bengali", jsonNode.get("Language").asText());
        });
    }

    @Test
    public void postRequestTest() {

        running( fakeApplication(), () -> {

            JsonNode bodyJson = JsonUtil.toJsonNodeFromJsonString("{\"name\":\"SIN\"}");

            RequestBuilder request = fakeRequest(POST,"/posttest").bodyJson(bodyJson);

            Result result = route(request);

            assertEquals(OK, result.status());

            JsonNode jsonNode = JsonUtil.toJsonNodeFromJsonString(contentAsString(result));

            assertEquals("SIN", jsonNode.get("Name").asText());
            assertEquals("3", jsonNode.get("Length").asText());
            assertEquals("S", jsonNode.get("StartsWith").asText());
        });
    }
}
