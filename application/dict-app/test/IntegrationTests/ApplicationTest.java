package IntegrationTests;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.typesafe.config.ConfigFactory;
import logics.WordLogic;
import objects.Word;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Result;
import play.test.WithServer;
import utilities.*;

import java.io.IOException;
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
    public void rootRouteTest() {
        running(fakeApplication(), () -> {
                Result result = route(fakeRequest(GET, "/"));
                assertEquals(OK, result.status());
                assertEquals("The Bangla Dictionary!",contentAsString(result));
            }
        );
    }

    @Test
    public void getRequestTest() {
        running(fakeApplication(), () -> {
            Result result = route(fakeRequest(GET, "/api/v1/gettest"));
            assertEquals(OK, result.status());
            JsonNode jsonNode = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            assertEquals("Dictionary", jsonNode.get("Application").asText());
            assertEquals("Bengali", jsonNode.get("Language").asText());
        });
    }

    @Test
    public void postRequestTest() {
        running( fakeApplication(), () -> {
            JsonNode bodyJson = JsonUtil.jsonStringToJsonNode("{\"name\":\"SIN\"}");
            Result result = route( fakeRequest(POST,"/api/v1/posttest").bodyJson(bodyJson) );
            assertEquals(OK, result.status());

            JsonNode jsonNode = JsonUtil.jsonStringToJsonNode(contentAsString(result));
            assertEquals("SIN", jsonNode.get("Name").asText());
            assertEquals("3", jsonNode.get("Length").asText());
            assertEquals("S", jsonNode.get("StartsWith").asText());
        });
    }

    @Test @Ignore //Ignore because it is not a functionality test
    public void tempTestConfig() {

        String configString = "shobdo.config";
        String config = ConfigFactory.load().getString(configString);
        String mongodbhostnameConfigString = "shobdo.mongodbhostname";
        String mongodbhostname = ConfigFactory.load().getString(mongodbhostnameConfigString);
        log.info("Config for \"" + configString + "\":" + config);
        log.info("Config for mongodbhostname\"" + mongodbhostnameConfigString + "\":" + mongodbhostname);
    }

    @Test @Ignore //Ignore because it is not a functionality test
    public void testGuava() throws IOException {

        List<Word> words = new ArrayList<>( DictUtil.generateRandomWordSet(2) );

        Word theWord = words.get(0);

        String spelling = theWord.getWordSpelling();

        WordLogic logic = WordLogic.createMongoBackedWordLogic();

        logic.createWord(theWord);

        LoadingCache<String, Word> wordLoadingCache = CacheBuilder.newBuilder()
                .expireAfterAccess(20, TimeUnit.MILLISECONDS)
                .expireAfterWrite(20, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, Word>() {
                    @Override
                    public Word load(String key) throws Exception {
                        WordLogic logic = WordLogic.createMongoBackedWordLogic();
                        return logic.getWordBySpelling(spelling);
                    }
                });

        for(int i = 0 ; i < 10 ; i++) {

            long start = System.currentTimeMillis();
            Word wordFromCache = logic.getWordBySpelling(spelling);
            log.info("Word From Cache, Spelling: " + wordFromCache.getWordSpelling());
            log.info("Word From Cache Time Taken:" + (System.currentTimeMillis() - start) + "ms");

            start = System.currentTimeMillis();
            try {
                Word wordFromOtherCache = wordLoadingCache.get(spelling);
                log.info("Word From Guava Cache, Spelling: " + wordFromOtherCache.getWordSpelling());
                log.info("Word From Guava Cache Time Taken:" + (System.currentTimeMillis() - start) + "ms");
            } catch (Exception ex) {
                log.info("Error getting object from guava cache");
            }
        }
    }

}
