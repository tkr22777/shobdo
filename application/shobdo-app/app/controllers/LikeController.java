package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import common.stores.MongoStoreFactory;
import like.LikeLogic;
import like.stores.LikeStoreMongoImpl;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.ShobdoLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LikeController extends Controller {

    private static final ShobdoLogger log = new ShobdoLogger(LikeController.class);
    private static LikeLogic likeLogic;

    public LikeController() {
        likeLogic = new LikeLogic(new LikeStoreMongoImpl(MongoStoreFactory.getLikesCollection()));
    }

    private Optional<String> getSessionUserId() {
        return Optional.ofNullable(session("userId"));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result likeWord() {
        final Optional<String> userIdOpt = getSessionUserId();
        if (!userIdOpt.isPresent()) {
            return unauthorized(Json.toJson(Collections.singletonMap("error", "Unauthorized")));
        }
        final JsonNode body = request().body().asJson();
        if (body == null || !body.has("wordId")) {
            return badRequest(Json.toJson(Collections.singletonMap("error", "wordId is required")));
        }
        final String wordId = body.get("wordId").asText();
        final String userId = userIdOpt.get();
        try {
            final boolean liked = likeLogic.toggleLike(userId, wordId);
            final Map<String, Object> response = new HashMap<>();
            response.put("liked", liked);
            response.put("wordId", wordId);
            return ok(Json.toJson(response));
        } catch (Exception ex) {
            log.error("@LC001 likeWord error userId:" + userId + " wordId:" + wordId, ex);
            return internalServerError(Json.toJson(Collections.singletonMap("error", "Failed to toggle like")));
        }
    }

    public Result unlikeWord(final String wordId) {
        final Optional<String> userIdOpt = getSessionUserId();
        if (!userIdOpt.isPresent()) {
            return unauthorized(Json.toJson(Collections.singletonMap("error", "Unauthorized")));
        }
        final String userId = userIdOpt.get();
        try {
            likeLogic.removeLike(userId, wordId);
            return ok();
        } catch (Exception ex) {
            log.error("@LC002 unlikeWord error userId:" + userId + " wordId:" + wordId, ex);
            return internalServerError(Json.toJson(Collections.singletonMap("error", "Failed to unlike")));
        }
    }

    public Result getLikedWords() {
        final Optional<String> userIdOpt = getSessionUserId();
        if (!userIdOpt.isPresent()) {
            return unauthorized(Json.toJson(Collections.singletonMap("error", "Unauthorized")));
        }
        final String userId = userIdOpt.get();
        try {
            final List<String> wordIds = likeLogic.getLikedWordIds(userId);
            return ok(Json.toJson(wordIds));
        } catch (Exception ex) {
            log.error("@LC003 getLikedWords error userId:" + userId, ex);
            return internalServerError(Json.toJson(Collections.singletonMap("error", "Failed to get liked words")));
        }
    }

    public Result getLikeCount(final String wordId) {
        try {
            final long count = likeLogic.getLikeCount(wordId);
            final Map<String, Object> response = new HashMap<>();
            response.put("wordId", wordId);
            response.put("count", count);
            return ok(Json.toJson(response));
        } catch (Exception ex) {
            log.error("@LC004 getLikeCount error wordId:" + wordId, ex);
            return internalServerError(Json.toJson(Collections.singletonMap("error", "Failed to get like count")));
        }
    }
}
