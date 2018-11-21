package objects;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

@Data @AllArgsConstructor @Builder
public abstract class EntityMeta {

    //Maybe have entity specific meta within the classes
    @NonNull private EntityStatus status = EntityStatus.ACTIVE;

    //TODO: You should have separate collections for each entity types
    protected EntityType type;

    //null for pioneer object, non null for updated object
    private String parentId;

    private String creatorId;
    private String creationDate;

    private String deleterId;
    private String deletedDate;

    //V1.5 validation of updates
    private String validatorId; //if validatorId is present, then the object has been validated
    private int version = 0;

    EntityMeta() { }

    public abstract JsonNode toJson();
}
