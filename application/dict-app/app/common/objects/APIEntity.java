package common.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface APIEntity {

    default JsonNode jsonNode() {
        return new ObjectMapper().convertValue(this, JsonNode.class);
    }
}
