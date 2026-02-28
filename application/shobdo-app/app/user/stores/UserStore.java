package user.stores;

import user.objects.User;

public interface UserStore {
    User getUserByGoogleId(String googleId);
    User createUser(User user);
}
