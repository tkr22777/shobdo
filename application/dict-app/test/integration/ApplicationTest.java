package integration;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.typesafe.config.ConfigFactory;
import common.store.MongoStoreFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithServer;
import utilities.ShobdoLogger;
import utilities.TestUtil;
import word.WordCache;
import word.WordLogic;
import word.WordStoreMongoImpl;
import word.objects.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApplicationTest extends WithServer {

    ShobdoLogger log;

    @Before
    public void setup() {
        log = new ShobdoLogger(ApplicationTest.class);
    }

    @Test
    public void rootRouteTest() {
        Helpers.running(Helpers.fakeApplication(), () -> {
                Result result = Helpers.route(Helpers.fakeRequest(Helpers.GET, "/api/v1"));
                Assert.assertEquals(Helpers.OK, result.status());
                Assert.assertEquals("বাংলা অভিধান এ স্বাগতম!", Helpers.contentAsString(result));
            }
        );
    }

    @Test
    public void testConfig() {
        Assert.assertEquals("27017",
            ConfigFactory.load().getString("shobdo.mongodb.port"));
        Assert.assertEquals("Dictionary",
            ConfigFactory.load().getString("shobdo.mongodb.database.dbname"));
        Assert.assertEquals("Words",
            ConfigFactory.load().getString("shobdo.mongodb.database.collection.words"));
        Assert.assertEquals("UserRequests",
            ConfigFactory.load().getString("shobdo.mongodb.database.collection.userrequests"));
    }

    @Test @Ignore //Ignore because it is not a functionality test
    public void testGuava() throws IOException {

        List<Word> words = new ArrayList<>( TestUtil.generateRandomWordSet(2) );

        Word theWord = words.get(0);
        String spelling = theWord.getSpelling();

        WordStoreMongoImpl storeMongo = new WordStoreMongoImpl(MongoStoreFactory.getWordCollection());
        WordLogic logic = new WordLogic(storeMongo, WordCache.getCache());

        logic.createWord(theWord);
        LoadingCache<String, Word> wordLoadingCache = CacheBuilder.newBuilder()
                .expireAfterAccess(20, TimeUnit.MILLISECONDS)
                .expireAfterWrite(20, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<String, Word>() {
                    @Override
                    public Word load(String key) throws Exception {
                        WordLogic logic = new WordLogic(storeMongo, WordCache.getCache());
                        return logic.getWordBySpelling(spelling);
                    }
                });

        for(int i = 0 ; i < 10 ; i++) {

            long start = System.currentTimeMillis();
            Word wordFromCache = logic.getWordBySpelling(spelling);
            log.info("Word From Cache, Spelling: " + wordFromCache.getSpelling());
            log.info("Word From Cache Time Taken:" + (System.currentTimeMillis() - start) + "ms");

            start = System.currentTimeMillis();
            try {
                Word wordFromOtherCache = wordLoadingCache.get(spelling);
                log.info("Word From Guava Cache, Spelling: " + wordFromOtherCache.getSpelling());
                log.info("Word From Guava Cache Time Taken:" + (System.currentTimeMillis() - start) + "ms");
            } catch (Exception ex) {
                log.info("Error getting object from guava cache");
            }
        }
    }

}
