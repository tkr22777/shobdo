package objects;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data @AllArgsConstructor @Builder
public class UserRequest extends EntityMeta {

    private String id;
    private RequestOperation operation;

    //The id's prefix should signify the type of the entity the request is intended for
    private String targetId;
    private JsonNode requestBody;
}
