package controllers;

import common.stores.MongoStoreFactory;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.ShobdoLogger;
import word.WordLogic;
import word.caches.WordCache;
import word.stores.WordStoreMongoImpl;

public class AdminController extends Controller {

    private static WordLogic wordLogic;
    private static final ShobdoLogger logger = new ShobdoLogger(AdminController.class);

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
        return ok("OK");
    }
}