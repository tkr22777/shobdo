package integration;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.typesafe.config.ConfigFactory;
import common.stores.MongoStoreFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.TestUtil;
import word.caches.WordCache;
import word.WordLogic;
import word.stores.WordStoreMongoImpl;
import word.objects.Word;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApplicationTest extends WithServer {

    Logger log;

    @Before
    public void setup() {
        log = LoggerFactory.getLogger(ApplicationTest.class);
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

}
