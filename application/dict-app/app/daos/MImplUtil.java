package daos;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import objects.EntityStatus;
import objects.UserRequest;
import objects.Word;
import org.bson.Document;
import utilities.JsonUtil;

/* package private */ class MImplUtil {

    public static final String DB_NAME = "Dictionary";
    public static final String ID_PARAM = "id";
    private static final String STATUS_PARAM = "status";

    private final static ObjectMapper objectMapper = new ObjectMapper()
        .configure(MapperFeature.USE_ANNOTATIONS, false);

    public static BasicDBObject getActiveObjectQuery() {
        final BasicDBObject query = new BasicDBObject();
        query.put(MImplUtil.STATUS_PARAM, EntityStatus.ACTIVE.toString());
        return query;
    }

    public static UserRequest toUserRequest(final Document doc) {
        doc.remove("_id");
        return (UserRequest) JsonUtil.jStringToObject(doc.toJson(), UserRequest.class);
    }

    public static Word toWord(final Document doc) {
        doc.remove("_id");
        return (Word) JsonUtil.jStringToObject(doc.toJson(), Word.class);
    }

    public static Document toDocument(Object object) {
        try {
            return Document.parse(objectMapper.writeValueAsString(object) );
        } catch (Exception ex) {
            throw new IllegalArgumentException("objectToDocument error. Object["
                + object.toString() + "][Ex:" + ex.getStackTrace().toString());
        }
    }
}
