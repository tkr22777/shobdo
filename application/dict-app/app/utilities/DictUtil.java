package utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import objects.DictionaryWord;
import objects.Meaning;
import objects.MeaningForPartsOfSpeech;
import objects.PartsOfSpeechSet;
import org.bson.Document;

import java.util.*;

/**
 * Created by tahsinkabir on 8/27/16.
 */
public class DictUtil {

    private static LogPrint log = new LogPrint(DictUtil.class);

    public static int randomInRange(int lowest, int highest){
        return new Random().nextInt( highest - lowest + 1) + lowest;
    }

    public static Object getObjectFromDocument(Document doc, Class<?> class_type) {

        doc.remove("_id");

        ObjectMapper mapper = new ObjectMapper();

        Object object = null;

        try {

            object = mapper.readValue( doc.toJson(), class_type );

        } catch ( Exception ex ){

            log.info( "Failed to map json word to dictionary word object. Ex: " + ex.getMessage() );
        }

        return object;
    }

    public static Set<DictionaryWord> generateDictionaryWithRandomWords(int numberOfWords){

        Set<DictionaryWord> words = new HashSet<>();

        for(int i = 0 ; i < numberOfWords ; i++) {

            DictionaryWord word = generateRandomWord( new PartsOfSpeechSet() );
            words.add(word);
        }

        return words;
    }

    public static DictionaryWord generateRandomWord(PartsOfSpeechSet partsOfSpeech ) {

        String start = "995"; //ক
        String end = "9A8";   //ন

        String wordSpelling;
        String wordId;

        int wordLength = DictUtil.randomInRange(2, 9);
        wordSpelling = Bangla.getWord(start, end, wordLength);
        wordId = "WD_" + UUID.randomUUID();

        DictionaryWord dictionaryWord = new DictionaryWord(wordId, wordSpelling);

        ArrayList<MeaningForPartsOfSpeech> meaningsForPartsOfSpeech = new ArrayList<>();

        for (String pos : partsOfSpeech.getPartsOfSpeeches()) {

            MeaningForPartsOfSpeech meanings = new MeaningForPartsOfSpeech();
            meanings.setType(pos);

            int numberOfMeaningForPOS = DictUtil.randomInRange(1,3);

            for(int j = 0; j < numberOfMeaningForPOS ; j++) {

                String meaning;
                wordLength = DictUtil.randomInRange(2, 9);
                meaning = Bangla.getWord(start, end, wordLength);

                String example;
                int preSentenceLen = DictUtil.randomInRange(2, 6);
                int postSentenceLen = DictUtil.randomInRange(2, 4);
                example = Bangla.getSentence(start, end, preSentenceLen, 12);
                example += " " + meaning + " ";
                example += Bangla.getSentence(start, end, postSentenceLen, 12);

                String meaningId = "MN_" + UUID.randomUUID();

                int strengh = DictUtil.randomInRange( 0 , 10);
                Meaning meaningForPOS = new Meaning(meaningId, pos, meaning, example, strengh);

                meanings.setAMeaning(meaningForPOS);

            }

            meaningsForPartsOfSpeech.add(meanings);

        }

        dictionaryWord.setMeaningForPartsOfSpeeches(meaningsForPartsOfSpeech);

        return dictionaryWord;
    }

}
