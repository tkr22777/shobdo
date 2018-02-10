package objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data @AllArgsConstructor
public class EntityMeta {

    //private String id;
    @NonNull private EntityStatus status = EntityStatus.ACTIVE;
    @NonNull private EntityType type = EntityType.UNKNOWN;

    private String parentId; //null for pioneer object
    @NonNull private String creatorId; //creator is the deleter of the parent if parentId is present

    @NonNull private String creationDate;
    private String deactivationDate;

    //V1.5 validation of updates
    private String validatorId; //if validatorId is present, then the object has been validated
    private int version = 0;
}
