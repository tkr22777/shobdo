package request;

import request.objects.UserRequest;
import java.util.ArrayList;

public interface UserRequestStore {

     UserRequest create(UserRequest request);

     UserRequest get(String id);

     UserRequest update(UserRequest request);

     void delete(String requestId);

     long count();

     ArrayList<UserRequest> list(String startId, int limit);
}
