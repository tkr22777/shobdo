package objects;

/**
 * Created by tahsinkabir on 6/16/16.
 */
public class Meaning {

    String id;
    String partOfSpeech; // the partOfSpeech of the meaning (redundant? since it will always be part of the higher category
    String meaning; //The meaning
    String example; //exmaple of the word used with the context of this meaning

    int strength; //how strongly does this meaning apply to the word invisible

    public Meaning(String id, String partOfSpeech, String meaning, String example) {
        this.id = id;
        this.partOfSpeech = partOfSpeech;
        this.meaning = meaning;
        this.example = example;
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
        return "Meaning{" +
                "id='" + id + '\'' +
                ", partOfSpeech='" + partOfSpeech + '\'' +
                ", meaning='" + meaning + '\'' +
                ", example='" + example + '\'' +
                ", strength=" + strength +
                '}';
    }
}