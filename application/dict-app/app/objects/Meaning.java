package objects;

import lombok.Data;
import utilities.JsonUtil;

/**
 * Created by tahsinkabir on 6/16/16.
 */
@Data
public class Meaning extends VersionMeta {

    private String meaningId;
    private String meaning;
    private String partOfSpeech;
    private String exampleSentence;

    private int strength = -1; //how strongly does this meaningsMap apply to the word, -1 means unset

    private VersionMeta versionMeta;

    @Override
    public String toString() {
        return JsonUtil.objectToJsonString(this);
    }
}