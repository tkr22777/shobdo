package objects;

import lombok.Builder;
import lombok.Data;
import utilities.JsonUtil;

/**
 * Created by Tahsin Kabir on 6/16/16.
 */
@Data @Builder
public final class Meaning extends EntityMeta {

    private String id;
    private String pronunciation; //uccharon
    private String origin; //but-potti
    private String partOfSpeech; //pod-porichoy
    private String meaning; //ortho
    private String exampleSentence;
    private String english; //poribhasha

    //how strongly does this meaning apply to the word, -1 means unset, higher is stronger
    private final int strength = -1;

    @Override
    public String toString() {
        return JsonUtil.objectToJsonString(this);
    }
}