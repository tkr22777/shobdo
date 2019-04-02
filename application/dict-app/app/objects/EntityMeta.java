package objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
/* package private */ abstract class EntityMeta {

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
}
