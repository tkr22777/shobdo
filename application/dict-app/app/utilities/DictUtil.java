package utilities;

import objects.Constants;
import objects.Word;
import objects.Meaning;

import java.util.*;
import java.util.stream.Collectors;

public class DictUtil {

    private static final ShobdoLogger log = new ShobdoLogger(DictUtil.class);

    private DictUtil() {
    }

    public static int randIntInRange(final int lowest, final int highest) {
        return new Random().nextInt(highest - lowest + 1) + lowest;
    }

    public static String generateWordId() {
        return String.format("%s-%s", Constants.WORD_ID_PREFIX, UUID.randomUUID());
    }

    public static Set<Meaning> genMeaning(final String wordSpelling, final int count){
        final Set<Meaning> meanings = new HashSet<>();
        for (int i = 0 ; i < count ; i++) {
            Meaning meaning = genAMeaning(wordSpelling);
            meanings.add(meaning);
        }
        return meanings;
    }

    private static Meaning genAMeaning(final String wordSpelling) {
        final String meaningString =  BanglaUtil.generateRandomSentence(3);
        final String exampleSentence =  BanglaUtil.generateSentenceWithWord(wordSpelling);
        return Meaning.builder()
            .meaning(meaningString)
            .exampleSentence(exampleSentence)
            .build();
    }

    public static Set<Word> generateRandomWordSet(int numberOfWords){
        final Set<Word> words = new HashSet<>();
        for (int i = 0 ; i < numberOfWords ; i++) {
            final Word word = genARandomWord();
            words.add(word);
        }
        return words;
    }

    /* does not add wordId */
    public static Word genARandomWord() {
        final int wordLength = randIntInRange(2, 22);
        return Word.builder()
            .spelling(BanglaUtil.generateRandomWordString(wordLength))
            .build();
    }

    public static void printStringsByTag(String tag, List<?> strings, int start, int limit, boolean randomize) {

        if (strings == null) {
            return;
        }

        List<?> toPrint = strings;
        if (randomize) {
            toPrint = new ArrayList<>(strings);
            Collections.shuffle(toPrint);
        }

        for (int i = start ; i < toPrint.size() && i <  start + limit ; i++) {
            log.info("#" + i + " " + tag + ": '"+ toPrint.get(i).toString() + "'");
        }
    }

    public static Map<String, Word> removeKeyValuesForKeys(Map<String, Word> map, Set<String> keys) {
        return map.entrySet().stream()
            .filter(e -> !keys.contains(e.getKey() ) )
            .collect(
                Collectors.toMap(
                    e -> e.getKey(),
                    e -> e.getValue()
                )
            );
    }

    public static Map<String, Word> filterForKeys(Map<String, Word> map, Set<String> keys) {
        return map.entrySet().stream()
            .filter(e -> keys.contains(e.getKey()))
            .collect(
                Collectors.toMap(
                    e->e.getKey(),
                    e->e.getValue()
                )
            );
    }
}
