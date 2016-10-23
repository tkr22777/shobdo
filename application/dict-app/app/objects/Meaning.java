package objects;

/**
 * Created by tahsinkabir on 6/16/16.
 */
public class Meaning {

    String id;
    String partOfSpeech; //The partOfSpeech of the meaning (redundant?)
    String meaning; //The meaning
    String example; //Example of the word used with the context of this meaning

    int strength; //how strongly does this meaning apply to the word, -1 means unset

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

        return customToString();
    }

    public String customToString() {

        return "Meaning {" +
                //"\n\n\t\t\t\tid = '" + id + '\'' +
                //"\n\n\t\t\t\tpartOfSpeech = '" + partOfSpeech + '\'' +
                "\n\n\t\t\t\tmeaning = '" + meaning + '\'' +
                //"\n\n\t\t\t\texample = '" + example + '\'' +
                //"\n\n\t\t\t\tstrength = " + strength +
                '}';
    }

}