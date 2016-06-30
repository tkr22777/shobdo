package objects;

/**
 * Created by tahsinkabir on 6/16/16.
 */
public class Word {

    String word_id;
    String word_spelling;

    public Word(String word_id, String word_spelling) {
        this.word_id = word_id;
        this.word_spelling = word_spelling;
    }

    public String getWord_id() {
        return word_id;
    }

    public void setWord_id(String word_id) {
        this.word_id = word_id;
    }

    public String getWord_spelling() {
        return word_spelling;
    }

    public void setWord_spelling(String word_spelling) {
        this.word_spelling = word_spelling;
    }

    @Override
    public String toString() {
        return "Word{" +
                "word_id='" + word_id + '\'' +
                ", word_spelling='" + word_spelling + '\'' +
                '}';
    }
}
