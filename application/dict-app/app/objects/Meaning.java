package objects;

import lombok.Data;
import utilities.Constants;
import utilities.JsonUtil;

/**
 * Created by tahsinkabir on 6/16/16.
 */
@Data
public class Meaning {

    int strength; //how strongly does this meanings apply to the word, -1 means unset
    String partOfSpeech; //The partOfSpeech of the meanings (redundant?)
    String meaning; //The meanings
    String example; //Example of the word used with the context of this meanings

    public Meaning() { }

    public Meaning(String partOfSpeech, String meaning, String example, int strength) {
        this.partOfSpeech = partOfSpeech;
        this.meaning = meaning;
        this.example = example;
        this.strength = strength;
    }

    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}