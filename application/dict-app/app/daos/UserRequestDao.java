package daos;

import objects.UserRequest;
import java.util.ArrayList;

public interface UserRequestDao {

     UserRequest create(UserRequest request);

     UserRequest get(String id);

     UserRequest update(UserRequest request);

     void delete(String requestId);

     long totalCount();

     ArrayList<UserRequest> list(String startId, int limit);
}
