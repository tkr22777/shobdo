package objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class SRequest {

    @JsonIgnore
    private String _id;

    private String requestId;
    private String targetId;
    private String targetType;
    private String operation;
    private JsonNode body;
    private VersionMeta versionMeta;
}
