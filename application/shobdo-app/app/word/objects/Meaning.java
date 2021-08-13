package word.objects;

import common.objects.APIEntity;
import lombok.*;
import utilities.JsonUtil;

@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Meaning implements APIEntity {

    private String id;
    private String meaning;
    private int strength;
    private String partOfSpeech;
    private String pronunciation;
    private String exampleSentence;

    public static Meaning fromMeaning(final Meaning meaning) {
        return (Meaning) JsonUtil.jNodeToObject(JsonUtil.objectToJNode(meaning), Meaning.class);
    }

    @Override
    public String toString() {
        return JsonUtil.objectToJString(this);
    }
}