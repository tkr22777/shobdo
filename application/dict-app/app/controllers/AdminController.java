package controllers;

import logics.WordLogic;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.ShobdoLogger;

/**
 * Created by Tahsin Kabir on 12/31/16.
 */
public class AdminController extends Controller {

    private static final WordLogic wordLogic = WordLogic.createMongoBackedWordLogic();
    private static final ShobdoLogger logger = new ShobdoLogger(AdminController.class);

    public Result flushCache() {
        logger.info("Flushing cache!");
        wordLogic.flushCache();
        return ok();
    }
}
