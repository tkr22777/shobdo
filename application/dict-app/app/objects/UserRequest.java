package objects;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data @AllArgsConstructor @Builder
public class UserRequest extends EntityMeta{

    private String requestId;
    private String targetId;
    private EntityType targetType;
    private RequestOperation operation;
    private JsonNode requestBody;
}
