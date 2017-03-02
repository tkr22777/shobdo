package IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.typesafe.config.ConfigFactory;
import logics.WordLogic;
import objects.DictionaryWord;
import objects.PartsOfSpeechSet;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Http.*;
import play.mvc.Result;
import play.test.WithServer;
import utilities.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

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

    @Before
    public void setup() {

        log = new LogPrint(ApplicationTest.class);
    }

    @Test
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
            Result result = route(fakeRequest(GET, "/gettest"));
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
            Result result = route( fakeRequest(POST,"/posttest").bodyJson(bodyJson) );

            assertEquals(OK, result.status());

            JsonNode jsonNode = JsonUtil.toJsonNodeFromJsonString(contentAsString(result));
            assertEquals("SIN", jsonNode.get("Name").asText());
            assertEquals("3", jsonNode.get("Length").asText());
            assertEquals("S", jsonNode.get("StartsWith").asText());
        });
    }

    @Test @Ignore //Ignore because it is not a functionality test
    public void tempTestConfig() {

        String configString = "shobdo.config";
        String config = ConfigFactory.load().getString(configString);
        log.info("Config for \"" + configString + "\":" + config);
    }

    @Test @Ignore //Ignore because it is not a functionality test
    public void testGuava() {

        List<DictionaryWord> words = new ArrayList<>( DictUtil.generateDictionaryWithRandomWords(2) );

        DictionaryWord theWord = words.get(0);

        String spelling = theWord.getWordSpelling();

        WordLogic logic = WordLogic.factory();

        logic.saveDictionaryWord(theWord);

        LoadingCache<String, DictionaryWord> dictionaryWordLoadingCache = CacheBuilder.newBuilder()
                .expireAfterAccess(20, TimeUnit.MILLISECONDS)
                .expireAfterWrite(20, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, DictionaryWord>() {
                    @Override
                    public DictionaryWord load(String key) throws Exception {
                        WordLogic logic = WordLogic.factory();
                        return logic.getDictionaryWordBySpelling(spelling);
                    }
                });

        for(int i = 0 ; i < 10 ; i++) {

            long start = System.currentTimeMillis();
            DictionaryWord wordFromCache = logic.getDictionaryWordBySpelling(spelling);
            log.info("Word From Cache, Spelling: " + wordFromCache.getWordSpelling());
            log.info("Word From Cache Time Taken:" + (System.currentTimeMillis() - start) + "ms");

            start = System.currentTimeMillis();
            try {
                DictionaryWord wordFromOtherCache = dictionaryWordLoadingCache.get(spelling);
                log.info("Word From Guava Cache, Spelling: " + wordFromOtherCache.getWordSpelling());
                log.info("Word From Guava Cache Time Taken:" + (System.currentTimeMillis() - start) + "ms");
            } catch (Exception ex) {
                log.info("Error getting object from guava cache");
            }
        }
    }

}
