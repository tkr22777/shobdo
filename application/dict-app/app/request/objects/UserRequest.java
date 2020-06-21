package request.objects;

import com.fasterxml.jackson.databind.JsonNode;
import common.objects.APIEntity;
import common.objects.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.Document;
import utilities.JsonUtil;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class UserRequest extends MongoEntity implements APIEntity {

    private String id; /* The id of the user request */
    private RequestOperation operation; /* the operation type of the request */

    private Map<TargetType, String> targetIds; /* the ids on the rest object mutation path */
    private TargetType targetType; /* Target type is required for create operation */
    private JsonNode requestBody;

    public static UserRequest fromBsonDoc(final Document doc) {
        doc.remove("_id");
        return (UserRequest) JsonUtil.jStringToObject(doc.toJson(), UserRequest.class);
    }
}
