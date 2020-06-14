package utilities;

import objects.Constants;
import objects.Meaning;
import objects.Word;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class DictUtil {

    private static final ShobdoLogger log = new ShobdoLogger(DictUtil.class);

    private DictUtil() {
    }

    public static int randIntInRange(final int lowest, final int highest) {
        return new Random().nextInt(highest - lowest + 1) + lowest;
    }

    public static String generateWordId() {
        return String.format("%s-%s", Constants.PREFIX_WORD_ID, UUID.randomUUID());
    }

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
        final int wordLength = randIntInRange(2, 22);
        return Word.builder()
            .spelling(BanglaUtil.generateRandomWordString(wordLength))
            .build();
    }
}
