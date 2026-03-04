package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import common.stores.MongoStoreFactory;
import exceptions.EntityDoesNotExist;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import user.UserLogic;
import user.objects.User;
import user.objects.UserRole;
import user.stores.UserStoreMongoImpl;
import utilities.ShobdoLogger;
import word.WordLogic;
import word.caches.WordCache;
import word.stores.WordStoreMongoImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminController extends Controller {

    private static WordLogic wordLogic;
    private static UserLogic userLogic;
    private static final ShobdoLogger logger = new ShobdoLogger(AdminController.class);

    public AdminController() {
        WordStoreMongoImpl wordStoreMongo = new WordStoreMongoImpl(MongoStoreFactory.getWordCollection());
        wordLogic = new WordLogic(wordStoreMongo, WordCache.getCache());
        userLogic = new UserLogic(new UserStoreMongoImpl(MongoStoreFactory.getUsersCollection()));
    }

    public Result flushCache() {
        if (!ControllerUtils.hasMinRole("ADMIN")) {
            return forbidden(Json.toJson(Collections.singletonMap("error", "Requires ADMIN role or above")));
        }
        logger.info("Flushing cache!");
        wordLogic.flushCache();
        return ok();
    }

    public Result health() {
        return ok("{ \"status\": \"OK\" }");
    }

    public Result listUsers() {
        if (!ControllerUtils.hasMinRole("ADMIN")) {
            return forbidden(Json.toJson(Collections.singletonMap("error", "Requires ADMIN role or above")));
        }
        try {
            final List<User> users = userLogic.listUsers();
            final List<Map<String, String>> response = new ArrayList<>();
            for (final User u : users) {
                final Map<String, String> entry = new HashMap<>();
                entry.put("id", u.getId());
                entry.put("name", u.getName() != null ? u.getName() : "");
                entry.put("email", u.getEmail() != null ? u.getEmail() : "");
                entry.put("role", u.getRole().name());
                response.add(entry);
            }
            return ok(Json.toJson(response));
        } catch (Exception ex) {
            logger.error("@ADC001 listUsers error", ex);
            return internalServerError(Json.toJson(Collections.singletonMap("error", "Failed to list users")));
        }
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result assignRole(final String userId) {
        if (!ControllerUtils.hasMinRole("ADMIN")) {
            return forbidden(Json.toJson(Collections.singletonMap("error", "Requires ADMIN role or above")));
        }
        final JsonNode body = request().body().asJson();
        if (body == null || !body.has("role")) {
            return badRequest(Json.toJson(Collections.singletonMap("error", "role is required")));
        }
        final String newRoleStr = body.get("role").asText();
        final UserRole newRole;
        try {
            newRole = UserRole.valueOf(newRoleStr);
        } catch (IllegalArgumentException e) {
            return badRequest(Json.toJson(Collections.singletonMap("error", "Invalid role: " + newRoleStr)));
        }
        final String callerRoleStr = session("userRole");
        final UserRole callerRole = callerRoleStr != null ? UserRole.valueOf(callerRoleStr) : UserRole.USER;
        try {
            final User updated = userLogic.assignRole(userId, newRole, callerRole);
            final Map<String, String> resp = new HashMap<>();
            resp.put("id", updated.getId());
            resp.put("role", updated.getRole().name());
            return ok(Json.toJson(resp));
        } catch (EntityDoesNotExist ex) {
            return notFound(Json.toJson(Collections.singletonMap("error", ex.getMessage())));
        } catch (IllegalArgumentException ex) {
            return forbidden(Json.toJson(Collections.singletonMap("error", ex.getMessage())));
        } catch (Exception ex) {
            logger.error("@ADC002 assignRole error", ex);
            return internalServerError(Json.toJson(Collections.singletonMap("error", "Failed to assign role")));
        }
    }
}
