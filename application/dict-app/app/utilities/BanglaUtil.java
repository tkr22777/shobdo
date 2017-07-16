package utilities;

import java.lang.Integer;
import java.lang.String;

public class BanglaUtil {

    public static String getBanglaRandomSentence(String startHex, String endHex, int numberOfWords, int maxWordLength) {

        if(startHex == null)
            startHex = "995"; //ক

        if(endHex == null)
            endHex = "9A8"; //ন

        if(numberOfWords == -1)
            numberOfWords = DictUtil.randomInRange(1, 10);

        if(maxWordLength < 1)
            maxWordLength = DictUtil.randomInRange(1, 7);

        String sentence = "";

        for(int i = 0 ; i < numberOfWords ; i++){

            if( i != 0)
                sentence += " ";

            int number = DictUtil.randomInRange(1, maxWordLength);
            String word = getBanglaRandomString(startHex, endHex, number);
            sentence += word;
        }

        return sentence;
    }

    public static String getBanglaRandomString(String startHex, String endHex, int wordLength) {

        int start = Integer.parseInt(startHex, 16);
        int end = Integer.parseInt(endHex, 16);

        String retString = "";

        if(wordLength < 1)
            wordLength = DictUtil.randomInRange(1, 10);

        for(int i = 0 ; i < wordLength ; i++) {

            int number = DictUtil.randomInRange(start, end);
            char c = (char) number;
            String single_char = "" + c;
            retString += single_char;
        }

        return retString;
    }
}
