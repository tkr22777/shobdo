package utilities;

import objects.Meaning;
import objects.Word;

public class WordUtil {

    public static Word addMeaningToWord(final Word word, final Meaning meaning) {
        if (word == null || meaning == null || meaning.getId() == null) {
            throw new RuntimeException("Word or Meaning is null");
        }
        word.getMeaningsMap().put(meaning.getId(), meaning);
        return  word;
    }
}
