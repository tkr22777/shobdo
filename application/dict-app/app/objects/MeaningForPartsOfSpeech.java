package objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import utilities.Constants;
import utilities.JsonUtil;
import utilities.LogPrint;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by tahsinkabir on 6/16/16.
 */
public class MeaningForPartsOfSpeech {

    String partsOfSpeech;
    ArrayList<Meaning> meanings = new ArrayList<>(); //the order of their meaning matters

    public MeaningForPartsOfSpeech() {

    }

    public MeaningForPartsOfSpeech(String partsOfSpeech, Collection<Meaning> meanings) {
        this.partsOfSpeech = partsOfSpeech;
        this.meanings = new ArrayList(meanings);
    }

    public String getPartsOfSpeech() {
        return partsOfSpeech;
    }

    public void setPartsOfSpeech(String partsOfSpeech) {
        this.partsOfSpeech = partsOfSpeech;
    }

    public ArrayList<Meaning> getMeanings() {
        return meanings;
    }

    public void setMeanings(ArrayList<Meaning> meanings) {
        this.meanings = meanings;
    }

    public void addMeaning(Meaning aMeaning) {

        if(meanings == null)
            meanings = new ArrayList<>();

        meanings.add(aMeaning);
    }

    public int generateCombinedStrength(){
        int total = 0;
        for(Meaning meaning: meanings)
            total += meaning.getStrength();
        return total;
    }

    public String toString() {

        if(Constants.JSON_STRING)
            return toJsonString();
        else
            return "MeaningForPartsOfSpeech{" +
                    "partsOfSpeech='" + partsOfSpeech + '\'' +
                    ", meanings=" + meanings +
                    '}';
    }

    public String toJsonString() {

        return JsonUtil.toJsonString(this);
    }

};
