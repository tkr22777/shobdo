package utilities;

import java.lang.Integer;
import java.lang.String;
import java.util.Random;

public class Bangla {

    public static String getSentence(String startHex, String endHex, int numberOfWords, int maxWordLength) {

        if(startHex == null)
            startHex = "995";

        if(endHex == null)
            endHex = "9A8";

        if(numberOfWords == -1)
            numberOfWords = randomInRange(1, 10);

        if(maxWordLength < 1)
            maxWordLength = randomInRange(1, 7);

        String sentence = "";

        for(int i = 0 ; i < numberOfWords ; i++){

            if( i != 0)
                sentence += " ";

            int number = randomInRange(1, maxWordLength);
            String word = getWord(startHex, endHex, number);
            sentence += word;
        }

        return sentence;
    }

    public static String getWord(String startHex, String endHex, int wordLength){

        int start = Integer.parseInt(startHex, 16);
        int end = Integer.parseInt(endHex, 16);

        String retString = "";

        if(wordLength < 1)
            wordLength = randomInRange(1, 10);

        for(int i = 0 ; i < wordLength ; i++){

            int number = randomInRange(start, end);
            char c = (char) number;
            String single_char = "" + c;
            retString += single_char;
        }

        return retString;
    }

    public static int randomInRange(int lowest, int highest){
        return new Random().nextInt( highest - lowest + 1) + lowest;
    }

}
