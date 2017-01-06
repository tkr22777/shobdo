package utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by tahsink on 1/6/17.
 */
public class JsonUtil {

    private static LogPrint log = new LogPrint(JsonUtil.class);

    public static Object toObjectFromJsonString(String jsonString, Class<?> class_type ) {

        Object toReturn = null;

        if (jsonString != null) {

            ObjectMapper mapper = new ObjectMapper();

            try {

                toReturn = mapper.readValue(jsonString, class_type);

            } catch (Exception ex) {

                log.info("@JU Error converting jsonString to Object. Exception:" + ex.getStackTrace().toString());

                return null;
            }
        }

        return toReturn;
    }

    public static String toJsonString(Object object) {

        String jsonString = null;

        try {

            jsonString = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);

        } catch (JsonProcessingException exception) {

            log.info("@JU001: Json Processing Exception Message: " + exception.getMessage());
        }

        return jsonString;
    }

    public static JsonNode toJsonNodeFromJsonString(String jsonString) {

        JsonNode jsonNode = null;

        if (jsonString != null) {

            ObjectMapper mapper = new ObjectMapper();

            try {

                jsonNode = mapper.readTree(jsonString);

            } catch (Exception ex) {

                log.info("Error converting jsonString to Object. Exception:" + ex.getStackTrace().toString());
                return null;
            }
        }

        return jsonNode;
    }
}
