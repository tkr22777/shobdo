package objects;

import lombok.Data;
import utilities.JsonUtil;

/**
 * Created by Tahsin Kabir on 6/16/16.
 */
@Data
public final class Meaning extends EntityMeta {

    private String id;
    private String meaning;
    private String partOfSpeech;
    private String exampleSentence;

    private int strength = -1; //how strongly does this meaningsMap apply to the word, -1 means unset

    @Override
    public String toString() {
        return JsonUtil.objectToJsonString(this);
    }
}