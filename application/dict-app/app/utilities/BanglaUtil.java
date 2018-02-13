package utilities;

import java.lang.Integer;
import java.lang.String;

public class BanglaUtil {

    public static String getBanglaRandomSentence(int numberOfWords, int maxWordLength) {

        String startHex = "995"; //ক
        String endHex = "9A8";   //ন

        if(numberOfWords < 1)
            numberOfWords = DictUtil.randomIntInRange(1, 10);

        if(maxWordLength < 1)
            maxWordLength = DictUtil.randomIntInRange(1, 7);

        String sentence = "";

        for(int i = 0 ; i < numberOfWords ; i++){

            if( i != 0)
                sentence += " ";

            int number = DictUtil.randomIntInRange(1, maxWordLength);
            String word = getRandomBanglaString(number);
            sentence += word;
        }

        return sentence;
    }

    public static String getRandomBanglaString(int wordLength) {

        String startHex = "995"; //ক
        String endHex = "9A8";   //ন

        int start = Integer.parseInt(startHex, 16);
        int end = Integer.parseInt(endHex, 16);

        String retString = "";

        if(wordLength < 1)
            wordLength = DictUtil.randomIntInRange(1, 10);

        for(int i = 0 ; i < wordLength ; i++) {

            int number = DictUtil.randomIntInRange(start, end);
            char c = (char) number;
            String single_char = "" + c;
            retString += single_char;
        }

        return retString;
    }

    public static String getRandomBanglaExampleString(String wordStringInSentence ) {
        int preSentenceLen = DictUtil.randomIntInRange(2, 6);
        int postSentenceLen = DictUtil.randomIntInRange(2, 4);
        return getBanglaRandomSentence(preSentenceLen, 12) + " " + wordStringInSentence + " "
                + getBanglaRandomSentence(postSentenceLen, 12);
    }
}
