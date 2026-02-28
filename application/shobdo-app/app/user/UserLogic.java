package user;

import user.objects.User;
import user.stores.UserStore;
import utilities.ShobdoLogger;

import java.util.UUID;

public class UserLogic {

    private static final String PREFIX_USER_ID = "USR";
    private static final ShobdoLogger log = new ShobdoLogger(UserLogic.class);

    private final UserStore userStore;

    public UserLogic(final UserStore userStore) {
        this.userStore = userStore;
    }

    public User findOrCreateUser(final String googleId, final String email, final String name) {
        User existing = userStore.getUserByGoogleId(googleId);
        if (existing != null) {
            log.debug("findOrCreateUser found existing user id:" + existing.getId());
            return existing;
        }
        final String userId = String.format("%s-%s", PREFIX_USER_ID, UUID.randomUUID());
        final User newUser = User.builder()
            .id(userId)
            .googleId(googleId)
            .email(email)
            .name(name)
            .build();
        log.debug("findOrCreateUser creating new user id:" + userId);
        return userStore.createUser(newUser);
    }

    public User getUserByGoogleId(final String googleId) {
        return userStore.getUserByGoogleId(googleId);
    }
}
