package objects;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data @AllArgsConstructor @Builder
public class UserRequest extends EntityMeta {

    private String id;
    private RequestOperation operation;

    private Map<TargetType, String> targetInfo;
    private TargetType targetType; //Target type is required creates as the targetInfo still does not exist
    private JsonNode requestBody;
}
