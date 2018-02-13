package helpers;

import objects.Meaning;
import objects.Word;

public class WordHelper {

    public static Word addMeaningToWord(Word word, Meaning meaning) {

        if(word == null || meaning == null || meaning.getMeaningId() == null)
            throw new RuntimeException("Word or Meaning in null");
        word.getMeaningsMap().put(meaning.getMeaningId(), meaning);
        return  word;
    }

}
