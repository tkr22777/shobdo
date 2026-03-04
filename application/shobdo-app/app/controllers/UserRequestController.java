package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import common.stores.MongoStoreFactory;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import request.UserRequestLogic;
import request.objects.UserRequest;
import request.stores.UserRequestStoreMongoImpl;
import utilities.ShobdoLogger;
import word.caches.WordCache;
import word.WordLogic;
import word.stores.WordStoreMongoImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class UserRequestController extends Controller {

    private static UserRequestLogic requestLogic;
    private static final ShobdoLogger logger = new ShobdoLogger(UserRequestController.class);

    public UserRequestController() {
        WordStoreMongoImpl wordStoreMongo = new WordStoreMongoImpl(MongoStoreFactory.getWordCollection());
        WordLogic wordLogic = new WordLogic(wordStoreMongo, WordCache.getCache());
        UserRequestStoreMongoImpl userStoreMongo = new UserRequestStoreMongoImpl(MongoStoreFactory.getUserRequestsCollection());
        requestLogic = new UserRequestLogic(wordLogic, userStoreMongo);
    }

    /** Returns the signed-in userId from the session, or null if not authenticated. */
    private String currentUserId() {
        return Optional.ofNullable(session("userId")).orElse(null);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createUserRequestForWordCreation() {
        final String submitterId = currentUserId();
        if (submitterId == null) {
            return unauthorized(Json.toJson(Collections.singletonMap("error", "Sign in required")));
        }
        final JsonNode wordJson = request().body().asJson();
        return ControllerUtils.executeEndpoint("", "", "WordCreationRequest", new HashMap<>(),
            () -> created(
                requestLogic.createUserRequestForWordCreation(wordJson, submitterId)
                    .jsonNode()
            )
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createUserRequestForWordUpdate(final String wordId) {
        final String submitterId = currentUserId();
        if (submitterId == null) {
            return unauthorized(Json.toJson(Collections.singletonMap("error", "Sign in required")));
        }
        final JsonNode wordJson = request().body().asJson();
        return ControllerUtils.executeEndpoint("", "", "WordUpdateRequest", new HashMap<>(),
            () -> ok(
                requestLogic.createUserRequestForWordUpdate(wordId, wordJson, submitterId)
                    .jsonNode()
            )
        );
    }

    public Result getUserRequest(final String requestId) {
        return ControllerUtils.executeEndpoint("", "", "GetUserRequest", new HashMap<>(),
            () -> ok(requestLogic.getRequest(requestId).jsonNode())
        );
    }

    public Result getMyRequests() {
        final String submitterId = currentUserId();
        if (submitterId == null) {
            return unauthorized(Json.toJson(Collections.singletonMap("error", "Sign in required")));
        }
        try {
            final List<UserRequest> requests = requestLogic.getRequestsBySubmitter(submitterId);
            return ok(Json.toJson(requests));
        } catch (Exception ex) {
            logger.error("@URC001 getMyRequests error", ex);
            return internalServerError(Json.toJson(Collections.singletonMap("error", "Failed to load requests")));
        }
    }

    public Result approveUserRequest(final String requestId) {
        if (!ControllerUtils.hasMinRole("REVIEWER")) {
            return forbidden(Json.toJson(Collections.singletonMap("error", "Requires REVIEWER role or above")));
        }
        return ControllerUtils.executeEndpoint("", "", "ApproveUserRequest", new HashMap<>(),
            () -> {
                requestLogic.approveUserRequest(requestId);
                return ok();
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createUserRequestForMeaningCreation(final String wordId) {
        final String submitterId = currentUserId();
        if (submitterId == null) {
            return unauthorized(Json.toJson(Collections.singletonMap("error", "Sign in required")));
        }
        final JsonNode meaningJson = request().body().asJson();
        return ControllerUtils.executeEndpoint("", "", "MeaningCreationRequest", new HashMap<>(),
            () -> created(
                requestLogic.createUserRequestForMeaningCreation(wordId, meaningJson, submitterId)
                    .jsonNode()
            )
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createUserRequestForMeaningUpdate(final String wordId) {
        final String submitterId = currentUserId();
        if (submitterId == null) {
            return unauthorized(Json.toJson(Collections.singletonMap("error", "Sign in required")));
        }
        final JsonNode meaningJson = request().body().asJson();
        return ControllerUtils.executeEndpoint("", "", "MeaningUpdateRequest", new HashMap<>(),
            () -> created(
                requestLogic.createUserRequestForMeaningUpdate(wordId, meaningJson, submitterId)
                    .jsonNode()
            )
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createUserRequestForMeaningDeletion(final String wordId, final String meaningId) {
        final String submitterId = currentUserId();
        if (submitterId == null) {
            return unauthorized(Json.toJson(Collections.singletonMap("error", "Sign in required")));
        }
        return ControllerUtils.executeEndpoint("", "", "MeaningDeletionRequest", new HashMap<>(),
            () -> created(
                requestLogic.createUserRequestForMeaningDeletion(wordId, meaningId, submitterId)
                    .jsonNode()
            )
        );
    }
}
