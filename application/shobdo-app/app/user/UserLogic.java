package user;

import exceptions.EntityDoesNotExist;
import user.objects.User;
import user.objects.UserRole;
import user.stores.UserStore;
import utilities.ShobdoLogger;

import java.util.List;
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

    public User getUserById(final String userId) {
        final User user = userStore.getUserById(userId);
        if (user == null) {
            throw new EntityDoesNotExist("User not found: " + userId);
        }
        return user;
    }

    public List<User> listUsers() {
        return userStore.listUsers();
    }

    public User assignRole(final String targetUserId, final UserRole newRole, final UserRole callerRole) {
        // ADMIN can assign USER or REVIEWER; OWNER can assign any role
        final int callerLevel = callerRole.ordinal(); // USER=0, REVIEWER=1, ADMIN=2, OWNER=3
        final int newRoleLevel = newRole.ordinal();
        if (callerLevel < UserRole.ADMIN.ordinal()) {
            throw new IllegalArgumentException("Insufficient privilege to assign roles");
        }
        if (newRoleLevel > callerLevel) {
            throw new IllegalArgumentException("Cannot assign a role higher than your own: " + newRole);
        }
        final User target = getUserById(targetUserId);
        target.setRole(newRole);
        log.debug("assignRole targetUserId:" + targetUserId + " newRole:" + newRole + " callerRole:" + callerRole);
        return userStore.updateUser(target);
    }
}
