package utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collection;

public class JsonUtil {

    private final static ShobdoLogger log = new ShobdoLogger(JsonUtil.class);
    private final static ObjectMapper objectMapper = new ObjectMapper()
        .configure(MapperFeature.USE_ANNOTATIONS, false);

    private JsonUtil() {
    }

    public static Object jStringToObject(String jsonString, Class<?> class_type ) {
        try {
            return objectMapper.readValue(jsonString, class_type);
        } catch (Exception ex) {
            log.error("@JU001: Error converting jsonString to Object.", ex);
            throw new IllegalArgumentException("JsonString to Object conversion failed:" + jsonString + " class type:" + class_type );
        }
    }

    public static JsonNode jStringToJNode(String jsonString) {
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception ex) {
            log.error("@JU003: Error converting jsonString to Object.", ex);
            throw new IllegalArgumentException("Invalid jsonString:" + jsonString);
        }
    }

    public static String objectToJString(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (Exception ex) {
            log.error("@JU002: Error converting object to json string.", ex);
            throw new IllegalArgumentException("Object to JsonString conversion failed:" + object );
        }
    }

    public static JsonNode objectToJNode(Object object) {
        try {
            return objectMapper.convertValue(object, JsonNode.class);
        } catch (Exception ex) {
            log.error("@JU004: Error converting Object to JsonNode.", ex);
            throw new IllegalArgumentException("Invalid Object:" + object);
        }
    }

    public static Object jNodeToObject(JsonNode jsonNode, Class<?> class_type) {
        try {
            return objectMapper.treeToValue(jsonNode, class_type);
        } catch (Exception ex)  {
            log.error("@JU005 exception while converting [JsonNode:" + jsonNode + "] to [ClassType:" + class_type + "]", ex);
            throw new IllegalArgumentException("Invalid JsonNode:" + jsonNode + " for ClassType:" + class_type);
        }
    }

    public static JsonNode nullFields(JsonNode node, Collection<String> attributes) {
        ObjectNode objectNode = node.deepCopy();
        for (String attribute: attributes) {
            objectNode.putNull(attribute);
        }
        return jStringToJNode(objectNode.toString());
    }
}
