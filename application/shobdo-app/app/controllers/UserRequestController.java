package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import common.stores.MongoStoreFactory;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import request.UserRequestLogic;
import request.stores.UserRequestStoreMongoImpl;
import utilities.ShobdoLogger;
import word.caches.WordCache;
import word.WordLogic;
import word.stores.WordStoreMongoImpl;

import java.util.HashMap;

public class UserRequestController extends Controller {

    private static UserRequestLogic requestLogic;
    private static final ShobdoLogger logger = new ShobdoLogger(UserRequestController.class);

    public UserRequestController() {
        WordStoreMongoImpl wordStoreMongo = new WordStoreMongoImpl(MongoStoreFactory.getWordCollection());
        WordLogic wordLogic = new WordLogic(wordStoreMongo, WordCache.getCache());
        UserRequestStoreMongoImpl userStoreMongo = new UserRequestStoreMongoImpl(MongoStoreFactory.getUserRequestsCollection());
        requestLogic = new UserRequestLogic(wordLogic, userStoreMongo);
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createUserRequestForWordCreation() {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        final JsonNode wordJson = request().body().asJson();
        return ControllerUtils.executeEndpoint(transactionId, requestId, "WordCreationRequest", new HashMap<>(),
            () -> created(
                requestLogic.createUserRequestForWordCreation(wordJson)
                    .jsonNode()
            )
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createUserRequestForWordUpdate(final String wordId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        final JsonNode wordJson = request().body().asJson();
        return ControllerUtils.executeEndpoint(transactionId, requestId, "WordUpdateRequest", new HashMap<>(),
            () -> ok(
                requestLogic.createUserRequestForWordUpdate(wordId, wordJson)
                    .jsonNode()
            )
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createUserRequestForWordDeletion(final String wordId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        final JsonNode wordJson = request().body().asJson();
        return ControllerUtils.executeEndpoint(transactionId, requestId, "WordDeletionRequest", new HashMap<>(),
            () -> created(
                requestLogic.createUserRequestForWordDeletion(wordId)
                    .jsonNode()
            )
        );
    }

    public Result getUserRequest(final String userRequestId) {
        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        final JsonNode wordJson = request().body().asJson();
        return ControllerUtils.executeEndpoint(transactionId, requestId, "GetUserRequest", new HashMap<>(),
            () -> ok(
                requestLogic.getRequest(userRequestId)
                    .jsonNode()
            )
        );
    }

    public Result approveUserRequest(final String userRequestId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        final JsonNode wordJson = request().body().asJson();
        return ControllerUtils.executeEndpoint(transactionId, requestId, "ApproveUserRequest", new HashMap<>(),
            () -> {
                requestLogic.approveUserRequest(userRequestId);
                return ok();
            }
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createUserRequestForMeaningCreation(final String wordId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        final JsonNode meaningJson = request().body().asJson();
        return ControllerUtils.executeEndpoint(transactionId, requestId, "MeaningCreationRequest", new HashMap<>(),
            () -> created(
                requestLogic.createUserRequestForMeaningCreation(wordId, meaningJson)
                    .jsonNode()
            )
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createUserRequestForMeaningUpdate(final String wordId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        final JsonNode meaningJson = request().body().asJson();
        return ControllerUtils.executeEndpoint(transactionId, requestId, "MeaningUpdateRequest", new HashMap<>(),
            () -> created(
                requestLogic.createUserRequestForMeaningUpdate(wordId, meaningJson)
                    .jsonNode()
            )
        );
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result createUserRequestForMeaningDeletion(final String wordId, final String meaningId) {

        final String transactionId = ""; // request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = ""; // request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "MeaningDeletionRequest", new HashMap<>(),
            () -> created(
                requestLogic.createUserRequestForMeaningDeletion(wordId, meaningId)
                    .jsonNode()
            )
        );
    }
}
