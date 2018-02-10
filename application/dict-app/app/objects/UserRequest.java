package objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class UserRequest {

    @JsonIgnore
    private String _id;

    private String requestId;
    private String targetId;
    private EntityType targetType;
    private RequestOperation operation;
    private JsonNode body;
    private EntityMeta entityMeta;
}