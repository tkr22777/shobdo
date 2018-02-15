package objects;

import lombok.Data;
import utilities.JsonUtil;

/**
 * Created by tahsinkabir on 6/16/16.
 */
@Data
public class Meaning {

    private String id;
    private String meaning;
    private String partOfSpeech;
    private String exampleSentence;

    private int strength = -1; //how strongly does this meaningsMap apply to the word, -1 means unset

    private EntityMeta entityMeta;

    @Override
    public String toString() {
        return JsonUtil.objectToJsonString(this);
    }
}