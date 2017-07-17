package objects;

import lombok.Data;
import utilities.Constants;
import utilities.JsonUtil;

import java.util.Date;

/**
 * Created by tahsinkabir on 6/16/16.
 */
@Data
public class Meaning {

    String meaningId;       //meaningId would be helpful for deleting/updating a specific meaning
    String meaning;         //the meanings
    String partOfSpeech;    //The part of speech of the meaning
    String example;         //example of the word in a sentence with the context of this meaning
    int strength = -1;      //how strongly does this meanings apply to the word, -1 means unset

    //For updates, we will set a deleted date on deletee meaning and
    //create a new meaning with the parentId = deletee.meaniningId
    Date deletedDate;
    String parentId;

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