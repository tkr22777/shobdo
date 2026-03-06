package word.objects;

import common.objects.MongoEntity;
import common.objects.APIEntity;
import lombok.*;
import org.bson.Document;
import utilities.JsonUtil;

/**
 * Lightweight lookup document stored in the InflectionIndex collection.
 * One document per inflected spelling — maps the surface form back to its root word.
 *
 * Example:
 *   { spelling: "আলোচনায়", rootId: "WD-abc", rootSpelling: "আলোচনা" }
 */
@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InflectionIndex extends MongoEntity implements APIEntity {

    private String id;

    /** The inflected surface form, e.g. "আলোচনায়" */
    private String spelling;

    /** ID of the root Word document */
    private String rootId;

    /** Spelling of the root word — denormalised for cheap display without a second fetch */
    private String rootSpelling;

    public static InflectionIndex fromBsonDoc(final Document doc) {
        doc.remove("_id");
        return (InflectionIndex) JsonUtil.jStringToObject(doc.toJson(), InflectionIndex.class);
    }

    @Override
    public String toString() {
        return JsonUtil.objectToJString(this);
    }
}
