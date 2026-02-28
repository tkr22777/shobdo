package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.typesafe.config.ConfigFactory;
import common.stores.MongoStoreFactory;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import user.UserLogic;
import user.objects.User;
import user.stores.UserStoreMongoImpl;
import utilities.ShobdoLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthController extends Controller {

    private static final ShobdoLogger log = new ShobdoLogger(AuthController.class);
    private static final String CLIENT_ID = ConfigFactory.load().getString("shobdo.google.clientId");
    private static UserLogic userLogic;

    public AuthController() {
        userLogic = new UserLogic(new UserStoreMongoImpl(MongoStoreFactory.getUsersCollection()));
    }

    @BodyParser.Of(BodyParser.Json.class)
    public Result googleSignIn() {
        final JsonNode body = request().body().asJson();
        if (body == null || !body.has("idToken")) {
            return badRequest(Json.toJson(Collections.singletonMap("error", "idToken is required")));
        }
        final String idTokenString = body.get("idToken").asText();
        try {
            final GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();
            final GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                return unauthorized(Json.toJson(Collections.singletonMap("error", "Invalid ID token")));
            }
            final GoogleIdToken.Payload payload = idToken.getPayload();
            final String googleId = payload.getSubject();
            final String email = payload.getEmail();
            final String name = (String) payload.get("name");
            final User user = userLogic.findOrCreateUser(googleId, email, name);
            session("userId", user.getId());
            session("userName", user.getName() != null ? user.getName() : "");
            session("userEmail", user.getEmail() != null ? user.getEmail() : "");
            final Map<String, String> responseMap = new HashMap<>();
            responseMap.put("id", user.getId());
            responseMap.put("name", user.getName());
            responseMap.put("email", user.getEmail());
            return ok(Json.toJson(responseMap));
        } catch (Exception ex) {
            log.error("@AC001 googleSignIn error", ex);
            return internalServerError(Json.toJson(Collections.singletonMap("error", "Authentication failed")));
        }
    }

    public Result logout() {
        session().clear();
        return ok();
    }

    public Result me() {
        final Optional<String> userIdOpt = Optional.ofNullable(session("userId"));
        if (!userIdOpt.isPresent()) {
            return unauthorized(Json.toJson(Collections.singletonMap("error", "Not signed in")));
        }
        final String userId = userIdOpt.get();
        try {
            // We store userId in the session; reconstruct a minimal user object for the response
            // The userId itself carries enough info (id). To return name/email we'd need a lookup.
            // Use a lightweight store lookup via googleId is not available here;
            // instead store name/email in session as well.
            final String name = session("userName");
            final String email = session("userEmail");
            final Map<String, String> responseMap = new HashMap<>();
            responseMap.put("id", userId);
            responseMap.put("name", name != null ? name : "");
            responseMap.put("email", email != null ? email : "");
            return ok(Json.toJson(responseMap));
        } catch (Exception ex) {
            log.error("@AC002 me error", ex);
            return internalServerError(Json.toJson(Collections.singletonMap("error", "Failed to get user")));
        }
    }
}
