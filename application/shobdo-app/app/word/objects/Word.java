package word.objects;

import common.objects.APIEntity;
import common.objects.MongoEntity;
import lombok.*;
import org.bson.Document;
import utilities.JsonUtil;

import java.util.HashMap;

@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Word extends MongoEntity implements APIEntity {

    private String id;
    private String spelling;

    //Easier to lookup/de-dupe following operations
    private HashMap<String, Meaning> meanings;

    public void putMeaning(final Meaning meaning) {
        if (meaning == null || meaning.getId() == null) {
            throw new IllegalArgumentException("Meaning or MeaningId is null");
        }
        if (getMeanings() == null) {
            setMeanings(new HashMap<>());
        }

        // Check for duplicate meaning texts for the same meaning id
        // getMeanings().values().stream()
        //     .filter(existing -> existing.getText().equals(meaning.getText()))
        //     .filter(existing -> !existing.getId().equals(meaning.getId()))
        //     .findFirst()
        //     .ifPresent(duplicate -> {
        //         throw new IllegalArgumentException(String.format(
        //             "A meaning with text '%s' already exists with a different ID. Existing meaning ID: '%s', New meaning ID: '%s'",
        //             meaning.getText(), duplicate.getId(), meaning.getId()
        //         ));
        //     });

        getMeanings().put(meaning.getId(), meaning);
    }

    public Meaning getMeaning(@NonNull final String meaningId) {
        if (getMeanings() == null) {
            setMeanings(new HashMap<>());
        }
        return getMeanings().get(meaningId);
    }

    public static Word fromBsonDoc(final Document doc) {
        doc.remove("_id");
        return (Word) JsonUtil.jStringToObject(doc.toJson(), Word.class);
    }

    public static Word fromWord(final Word word) {
        return (Word) JsonUtil.jNodeToObject(JsonUtil.objectToJNode(word), Word.class);
    }

    @Override
    public String toString() {
        return JsonUtil.objectToJString(this);
    }
}
