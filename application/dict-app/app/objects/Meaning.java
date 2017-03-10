package objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import utilities.Constants;
import utilities.JsonUtil;
import utilities.LogPrint;

/**
 * Created by tahsinkabir on 6/16/16.
 */
public class Meaning {

    String id;
    String partOfSpeech; //The partOfSpeech of the meanings (redundant?)
    String meaning; //The meanings
    String example; //Example of the word used with the context of this meanings
    int strength; //how strongly does this meanings apply to the word, -1 means unset

    public Meaning(){

    }

    public Meaning(String id, String partOfSpeech, String meaning, String example, int strength) {
        this.id = id;
        this.partOfSpeech = partOfSpeech;
        this.meaning = meaning;
        this.example = example;
        this.strength = strength;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    @Override
    public String toString() {

        if(Constants.JSON_STRING)
            return toJsonString();
        else
            return "Meaning{" +
                    "id='" + id + '\'' +
                    ", partOfSpeech='" + partOfSpeech + '\'' +
                    ", meanings='" + meaning + '\'' +
                    ", example='" + example + '\'' +
                    ", strength=" + strength +
                    '}';
    }

    public String toJsonString() {

        return JsonUtil.toJsonString(this);
    }
}