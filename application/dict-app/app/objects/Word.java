package objects;

/**
 * Created by tahsinkabir on 6/16/16.
 */
public class Word {

    String wordId;
    String wordSpelling;

    public Word(String wordId, String wordSpelling) {
        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
    }

    public String getWordId() {
        return wordId;
    }

    public void setWordId(String wordId) {
        this.wordId = wordId;
    }

    public String getWordSpelling() {
        return wordSpelling;
    }

    public void setWordSpelling(String wordSpelling) {
        this.wordSpelling = wordSpelling;
    }

    @Override
    public String toString() {
        return "Word{" +
                "wordId='" + wordId + '\'' +
                ", wordSpelling='" + wordSpelling + '\'' +
                '}';
    }
}
