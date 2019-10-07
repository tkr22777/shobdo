package objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import utilities.JsonUtil;

/**
 * Created by Tahsin Kabir on 6/16/16.
 */
@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Meaning extends EntityMeta {

    private String id;
    private String partOfSpeech; //pod-porichoy
    private String meaning; //ortho
    private String exampleSentence;
    //private String pronunciation; //uccharon
    //private String origin; //but-potti
    //private String english; //poribhasha

    //how strongly does this meaning apply to the word, 0 means unset, higher is stronger
    private int strength;

    public JsonNode toAPIJsonNode() {
        return new ObjectMapper().convertValue(this, JsonNode.class);
    }

    public static Meaning fromMeaning(final Meaning meaning) {
        return (Meaning) JsonUtil.jNodeToObject(JsonUtil.objectToJNode(meaning), Meaning.class);
    }

    @Override
    public String toString() {
        return JsonUtil.objectToJString(this);
    }
}