package daos;

import com.mongodb.BasicDBObject;
import objects.EntityStatus;
import objects.UserRequest;
import objects.Word;
import org.bson.Document;
import utilities.JsonUtil;

/* package private */ class MongoImplUtil {

    public static final String DB_NAME = "Dictionary";
    public static final String ID_PARAM = "id";
    private static final String STATUS_PARAM = "status";

    public static BasicDBObject getActiveObjectQuery() {
        final BasicDBObject query = new BasicDBObject();
        query.put(MongoImplUtil.STATUS_PARAM, EntityStatus.ACTIVE.toString());
        return query;
    }

    public static UserRequest getUserRequestFromDocument(final Document dictionaryDocument, final Class<?> class_type) {
        dictionaryDocument.remove("_id");
        return (UserRequest) JsonUtil.documentToObject(dictionaryDocument, class_type);
    }

    public static Word getWordFromDocument(final Document dictionaryDocument, final Class<?> class_type) {
        dictionaryDocument.remove("_id");
        return (Word) JsonUtil.documentToObject(dictionaryDocument, class_type);
    }
}
