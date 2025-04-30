package controllers;

import common.stores.MongoStoreFactory;
import play.mvc.Controller;
import play.mvc.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import word.WordLogic;
import word.caches.WordCache;
import word.stores.WordStoreMongoImpl;

public class AdminController extends Controller {

    private static WordLogic wordLogic;
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    public AdminController() {
        WordStoreMongoImpl wordStoreMongo = new WordStoreMongoImpl(MongoStoreFactory.getWordCollection());
        wordLogic = new WordLogic(wordStoreMongo, WordCache.getCache());
    }

    public Result flushCache() {
        logger.info("Flushing cache!");
        wordLogic.flushCache();
        return ok();
    }

    public Result health() {
        return ok("{ \"status\": \"OK\" }");
    }
}