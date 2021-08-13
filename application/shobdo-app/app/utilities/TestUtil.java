package utilities;

import word.objects.Meaning;
import word.objects.Word;

import java.util.HashSet;
import java.util.Set;

public class TestUtil {

    public static Set<Meaning> generateMeanings(final String spelling, final int count){
        final Set<Meaning> meanings = new HashSet<>();
        for (int i = 0 ; i < count ; i++) {
            Meaning meaning = generateMeaning(spelling);
            meanings.add(meaning);
        }
        return meanings;
    }

    private static Meaning generateMeaning(final String spelling) {
        final String meaningString =  BanglaUtil.generateRandomSentence(3);
        final String exampleSentence =  BanglaUtil.generateSentenceWithWord(spelling);
        return Meaning.builder()
            .meaning(meaningString)
            .exampleSentence(exampleSentence)
            .build();
    }


    public static Set<Word> generateRandomWordSet(int count){
        final Set<Word> words = new HashSet<>();
        for (int i = 0 ; i < count ; i++) {
            final Word word = generateRandomWord();
            words.add(word);
        }
        return words;
    }

    /* does not add wordId */
    public static Word generateRandomWord() {
        final int wordLength = DictUtil.randIntInRange(2, 22);
        return Word.builder()
            .spelling(BanglaUtil.generateRandomWordString(wordLength))
            .build();
    }
}

