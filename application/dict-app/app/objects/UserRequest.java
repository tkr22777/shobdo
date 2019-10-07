package objects;

import com.fasterxml.jackson.databind.JsonNode;
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

    private String id;
    private RequestOperation operation;

    private Map<TargetType, String> targetInfo;
    private TargetType targetType; //Target type is required creates as the targetInfo still does not exist
    private JsonNode requestBody;
}
