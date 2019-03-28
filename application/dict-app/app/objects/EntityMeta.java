package objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data @AllArgsConstructor @Builder
/* package private */ abstract class EntityMeta {

    @NonNull private EntityStatus status = EntityStatus.ACTIVE;

    private String creatorId;
    private String creationDate;

    private String deleterId;
    private String deletedDate;

    EntityMeta() { }
}
