package common.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.bson.Document;

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
    private String deletedDate;

    public Document document() {
        try {
            ObjectMapper mapper = new ObjectMapper().configure(MapperFeature.USE_ANNOTATIONS, false);
            return Document.parse(mapper.writeValueAsString(this));
        } catch (Exception ex) {
            throw new IllegalArgumentException("objectToDocument error. Object["
                + this.toString() + "][Ex:" + ex.getStackTrace().toString());
        }
    }
}
