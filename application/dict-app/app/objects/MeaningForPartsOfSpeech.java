package objects;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by tahsinkabir on 6/16/16.
 */
public class MeaningForPartsOfSpeech {

    String type; //type refers to the partOfSpeech
    ArrayList<Meaning> meanings = new ArrayList<>(); //the order of their meaning matters

    public MeaningForPartsOfSpeech() {

    }

    public MeaningForPartsOfSpeech(String type, Collection<Meaning> meanings) {
        this.type = type;
        this.meanings = new ArrayList(meanings);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public void setAMeaning(Meaning meaning) {

        if (meanings == null)
            meanings = new ArrayList<>();

        meanings.add(meaning);

    }

    public int generateCombinedStrength(){
        int total = 0;
        for(Meaning meaning: meanings)
            total += meaning.getStrength();
        return total;
    }

    @Override
    public String toString() {
        return customToString();
    }

    public String customToString() {

        return " MeaningForPartsOfSpeech {" +
                "\n\t\t\ttype = " + type + '\'' +
                "\n\t\t\tmeanings = " + meanings +
                "\n}";
    }

};
