package common.objects;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;

public interface MongoEntity {

    default Document document() {
        try {
            ObjectMapper mapper = new ObjectMapper().configure(MapperFeature.USE_ANNOTATIONS, false);
            return Document.parse(mapper.writeValueAsString(this));
        } catch (Exception ex) {
            throw new IllegalArgumentException("objectToDocument error. Object["
                + this.toString() + "][Ex:" + ex.getStackTrace().toString());
        }
    }
}
