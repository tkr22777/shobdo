package like.objects;

import common.objects.MongoEntity;
import lombok.*;
import org.bson.Document;
import utilities.JsonUtil;

@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Like extends MongoEntity {

    private String id;
    private String userId;
    private String wordId;

    public static Like fromBsonDoc(final Document doc) {
        doc.remove("_id");
        return (Like) JsonUtil.jStringToObject(doc.toJson(), Like.class);
    }

    @Override
    public String toString() {
        return JsonUtil.objectToJString(this);
    }
}
