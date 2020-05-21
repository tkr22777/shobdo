package objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class UserRequest extends EntityMeta {

    private String id; /* The id of the user request */
    private RequestOperation operation; /* the operation type of the request */

    private Map<TargetType, String> targetIds; /* the ids on the rest object mutation path */
    private TargetType targetType; /* Target type is required for create operation */
    private JsonNode requestBody;

    public JsonNode toAPIJsonNode() {
        return new ObjectMapper().convertValue(this, JsonNode.class);
    }
}
