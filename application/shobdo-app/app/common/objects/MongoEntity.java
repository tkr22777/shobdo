package common.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import lombok.*;
import org.bson.Document;
import utilities.Constants;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public abstract class MongoEntity {

    @JsonIgnore
    @NonNull private EntityStatus status = EntityStatus.ACTIVE;

    @JsonIgnore
    private String creatorId;
    @JsonIgnore
    private String creationDate;

    @JsonIgnore
    private String deleterId;
    @JsonIgnore
    private String deletionDate;

    public Document toDocument() {
        try {
            // TODO: note how does the following object mapper config work?
            ObjectMapper mapper = new ObjectMapper().configure(MapperFeature.USE_ANNOTATIONS, false);
            return Document.parse(mapper.writeValueAsString(this));
        } catch (Exception ex) {
            throw new IllegalArgumentException("objectToDocument error. Object["
                + this.toString() + "][Ex:" + ex.getStackTrace().toString());
        }
    }

    public Document toDeletedDocument() {
        final Document document = this.toDocument();
        document.put(Constants.MONGO_DOC_KEY_STATUS, EntityStatus.DELETED.toString());
        return document;
    }

    public static BasicDBObject getActiveObjectQuery() {
        final BasicDBObject query = new BasicDBObject();
        query.put(Constants.MONGO_DOC_KEY_STATUS, EntityStatus.ACTIVE.toString());
        return query;
    }

    public static BasicDBObject getActiveObjectQueryForId(String id) {
        final BasicDBObject query = getActiveObjectQuery();
        query.put(Constants.MONGO_DOC_KEY_ID, id);
        return query;
    }
}
