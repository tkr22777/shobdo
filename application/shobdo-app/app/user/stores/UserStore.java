package user.stores;

import user.objects.User;

import java.util.List;

public interface UserStore {
    User getUserByGoogleId(String googleId);
    User createUser(User user);
    User getUserById(String userId);
    User updateUser(User user);
    List<User> listUsers();
}
