package utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import objects.DictionaryWord;

/**
 * Created by tahsink on 1/6/17.
 */
public class JsonUtil {

    private static LogPrint log = new LogPrint(JsonUtil.class);

    public static Object toObjectFromJsonString(String jsonString, Class<?> class_type ) {

        if(jsonString == null || class_type == null)
            return null;

        ObjectMapper mapper = new ObjectMapper();

        try {

            return mapper.readValue(jsonString, class_type);

        } catch (Exception ex) {

            log.info("@JU001: Error converting jsonString to Object. Exception:" + ex.getStackTrace().toString());
            return null;
        }
    }

    public static String toJsonString(Object object) {

        if(object == null)
            return null;

        try {

            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);

        } catch (Exception ex) {

            log.info("@JU002: Error converting object to json string. Exception: " + ex.getStackTrace().toString());
            return null;
        }
    }

    public static JsonNode toJsonNodeFromJsonString(String jsonString) {

        if (jsonString == null)
            return null;

        ObjectMapper mapper = new ObjectMapper();

        try {

            return mapper.readTree(jsonString);

        } catch (Exception ex) {

            log.info("@JU003: Error converting jsonString to Object. Exception:" + ex.getStackTrace().toString());
            return null;
        }
    }

    public static Object jsonNodeToObject( JsonNode jsonNode, Class<?> class_type ) {

        if(jsonNode == null || class_type == null)
            return null;

        ObjectMapper jsonObjectmapper = new ObjectMapper();

        try {

            return jsonObjectmapper.treeToValue(jsonNode, class_type);

        } catch (JsonProcessingException ex) {

            log.info("@JU004: Error converting jsonNode to Object. Exception:" + ex.getStackTrace().toString());
            return null;
        }

    }
}
