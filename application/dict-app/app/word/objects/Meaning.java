package word.objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import common.objects.EntityMeta;
import utilities.JsonUtil;

@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Meaning extends EntityMeta {

    private String id;
    private String meaning;
    private int strength;
    private String partOfSpeech;
    private String pronunciation;
    private String exampleSentence;

    public static Meaning fromMeaning(final Meaning meaning) {
        return (Meaning) JsonUtil.jNodeToObject(JsonUtil.objectToJNode(meaning), Meaning.class);
    }

    public JsonNode toAPIJNode() {
        return new ObjectMapper().convertValue(this, JsonNode.class);
    }

    @Override
    public String toString() {
        return JsonUtil.objectToJString(this);
    }
}