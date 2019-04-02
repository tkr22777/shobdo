package objects;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data @AllArgsConstructor @Builder
public class UserRequest extends EntityMeta {

    private String id;
    private RequestOperation operation;

    private String targetId;
    private TargetType targetType; //Target type is required creates as the targetId still does not exist
    private JsonNode requestBody;
}
