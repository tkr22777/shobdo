package user.objects;

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
public class User extends MongoEntity {

    private String id;
    private String googleId;
    private String email;
    private String name;

    public static User fromBsonDoc(final Document doc) {
        doc.remove("_id");
        return (User) JsonUtil.jStringToObject(doc.toJson(), User.class);
    }

    @Override
    public String toString() {
        return JsonUtil.objectToJString(this);
    }
}
