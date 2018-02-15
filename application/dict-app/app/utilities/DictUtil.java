package utilities;

import objects.UserRequest;
import objects.Word;
import objects.Meaning;
import objects.PartsOfSpeechSet;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tahsinkabir on 8/27/16.
 */
public class DictUtil {

    private static LogPrint log = new LogPrint(DictUtil.class);

    public static int randomIntInRange(int lowest, int highest) {
        return new Random().nextInt( highest - lowest + 1) + lowest;
    }

    public static UserRequest getRequestFromDocument(Document dictionaryDocument, Class<?> class_type) {

        dictionaryDocument.remove("_id");
        return (UserRequest) JsonUtil.documentToObject(dictionaryDocument, class_type);
    }

    public static Word getWordFromDocument(Document dictionaryDocument, Class<?> class_type) {

        dictionaryDocument.remove("_id");
        return (Word) JsonUtil.documentToObject(dictionaryDocument, class_type);
    }

    public static Set<Meaning> generateRandomMeaning(String wordSpelling, int numberOfMeanings){

        Set<Meaning> meanings = new HashSet<>();

        for(int i = 0 ; i < numberOfMeanings ; i++) {
            Meaning meaning = generateARandomMeaning(wordSpelling);
            meanings.add(meaning);
        }

        return meanings;
    }

    private static Meaning generateARandomMeaning(String wordSpelling) {

        Meaning meaning = new Meaning();

        String meaningString =  BanglaUtil.getBanglaRandomSentence(3, 5);
        meaning.setMeaning(meaningString);

        String exampleSentence =  BanglaUtil.getRandomBanglaExampleString(wordSpelling);
        meaning.setExampleSentence(exampleSentence);

        meaning.setPartOfSpeech(new PartsOfSpeechSet().getPartsOfSpeeches().iterator().next());

        return meaning;
    }

    public static Set<Word> generateRandomWordSet(int numberOfWords){

        Set<Word> words = new HashSet<>();

        for(int i = 0 ; i < numberOfWords ; i++) {

            Word word = generateARandomWord();
            words.add(word);
        }

        return words;
    }

    private static Word generateARandomWord() {

        int wordLength = randomIntInRange(2, 9);

        Word word = new Word();
        word.setWordSpelling(BanglaUtil.getRandomBanglaString(wordLength));

        return word;
    }

    public static Word generateARandomWord(PartsOfSpeechSet partsOfSpeech ) {

        int wordLength = randomIntInRange(2, 9);
        String wordSpelling = BanglaUtil.getRandomBanglaString(wordLength);

        Word word = new Word();
        word.setWordSpelling(wordSpelling);

        HashMap<String,Meaning> meaningsMap = new HashMap<>();

        for (String partOfSpeech : partsOfSpeech.getPartsOfSpeeches()) {

            int numberOfMeaningForPOS = randomIntInRange(1,3);

            for(int j = 0; j < numberOfMeaningForPOS ; j++) {

                wordLength = randomIntInRange(2, 9);

                String meaningString = BanglaUtil.getRandomBanglaString(wordLength);
                String exampleSentence = BanglaUtil.getRandomBanglaExampleString(meaningString);
                int strength = randomIntInRange(0 , 10);
                //Meaning meaning = new Meaning(partOfSpeech, meaningString, exampleSentence, strength);

                //meaningsMap.put(meaning.getId(), meaning);
            }
        }

        word.setMeaningsMap(meaningsMap);

        return word;
    }

    public static void printStringsByTag(String tag, List<?> strings, int start, int limit, boolean randomize) {

        if(strings == null)
            return;

        List<?> toPrint = strings;

        if(randomize) {
            toPrint = new ArrayList<>(strings);
            Collections.shuffle(toPrint);
        }

        for(int i = start ; i < toPrint.size() && i <  start + limit ; i++) {
            log.info( "#" + i + " " + tag + ": '"+ toPrint.get(i).toString() + "'");
        }
    }

    public static Map<String, Word> removeKeyValuesForKeys(Map<String, Word> map, Set<String> keys) {

        return map.entrySet().stream()
                .filter( e -> !keys.contains( e.getKey() ) )
                .collect(
                        Collectors.toMap(
                                e -> e.getKey(),
                                e -> e.getValue()
                        )
                );
    }

    public static Map<String, Word> filterForKeys(Map<String, Word> map, Set<String> keys) {

        return map.entrySet().stream()
                .filter( e -> keys.contains(e.getKey()))
                .collect(
                        Collectors.toMap(
                                e->e.getKey(),
                                e->e.getValue()
                        )
                );
    }
}
