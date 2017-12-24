package utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.ByteBuffer;
import java.util.Collection;

/**
 * Created by tahsink on 1/6/17.
 */
public class JsonUtil {

    private static LogPrint log = new LogPrint(JsonUtil.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static Object toObjectFromJsonString(String jsonString, Class<?> class_type ) {

        try {

            return objectMapper.readValue(jsonString, class_type);

        } catch (Exception ex) {

            log.info("@JU001: Error converting jsonString to Object. Exception:" + ex.getStackTrace().toString());
            throw new IllegalArgumentException("JsonString to Object conversion failed:" + jsonString + " class type:" + class_type );
        }
    }

    public static String toJsonString(Object object) {

        try {

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);

        } catch (Exception ex) {

            log.info("@JU002: Error converting object to json string. Exception: " + ex.getStackTrace().toString());
            throw new IllegalArgumentException("Object to JsonString conversion failed:" + object );
        }
    }

    public static JsonNode toJsonNodeFromJsonString(String jsonString) {

        try {

            return objectMapper.readTree(jsonString);

        } catch (Exception ex) {

            log.info("@JU003: Error converting jsonString to Object. Exception:" + ex.getStackTrace().toString());
            throw new IllegalArgumentException("Invalid jsonString:" + jsonString);
        }
    }

    public static JsonNode toJsonNodeFromObject(Object object) {

        try {

            return objectMapper.convertValue(object, JsonNode.class);

        } catch (Exception ex) {

            log.info("@JU004: Error converting Object to JsonNode. Exception:" + ex.getStackTrace().toString());
            throw new IllegalArgumentException("Invalid Object:" + object);
        }
    }

    public static ByteBuffer jsonNodeToByteBuffer(JsonNode jsonNode) {

        try {

            return ByteBuffer.wrap(objectMapper.writeValueAsBytes(jsonNode));

        } catch (Exception ex) {

            log.info("@JU005 exception while converting [JsonNode:" + jsonNode + "] to ByteBuffer ");
            throw new IllegalArgumentException("Invalid JsonNode:" + jsonNode);
        }
    }

    public static Object jsonNodeToObject( JsonNode jsonNode, Class<?> class_type ) {

        try {

            return objectMapper.treeToValue(jsonNode, class_type);

        } catch (Exception ex)  {

            log.info("@JU005 exception while converting [JsonNode:" + jsonNode + "] to [ClassType:" + class_type + "]");
            throw new IllegalArgumentException("Invalid JsonNode:" + jsonNode + " for ClassType:" + class_type);
        }
    }

    public static JsonNode removeFieldsFromJsonNode(JsonNode node, Collection<String> attributes) {

        ObjectNode objectNode = node.deepCopy();
        for(String attribute: attributes) {
            objectNode.remove(attribute);
        }
        return toJsonNodeFromJsonString(objectNode.toString());
    }

    public static JsonNode nullFieldsFromJsonNode(JsonNode node, Collection<String> attributes) {

        ObjectNode objectNode = node.deepCopy();
        for(String attribute: attributes) {
            objectNode.putNull(attribute);
        }
        return toJsonNodeFromJsonString(objectNode.toString());
    }
}
