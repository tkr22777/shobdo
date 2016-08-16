import java.lang.Integer;
import java.lang.String;
import java.lang.System;
import java.util.Random;

public class Bangla{

    public static void main(String args[]){

        String start = "995"; //ক
        String end = "9A8"; //ন

        int WORDS_TO_GENERATE = 5;

        for(int i = 0 ; i < WORDS_TO_GENERATE ; i++){

            int number = randomInRange( 2 , 9);
            String word = getWord(start, end, number);
            print("Word " + i + " : " + word);
        }

        int SENTENCES_TO_GENERATE = 5;

        for(int i = 0 ; i < SENTENCES_TO_GENERATE ; i++ ){
            int number = randomInRange( 4 , 12);
            String sentence = getSentence(start, end, number, 12);
            print("Sentence " + i + " : " + sentence);
        }

    }

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

    public static void printAllInRange(String startHex, String endHex){

        int start = Integer.parseInt(startHex, 16);
        int end = Integer.parseInt(endHex, 16);

        String retString = "";

        while(start <= end){

            char c = (char) start;
            String single_char = "Number: " + start + " Char:" + c;
            print(single_char);
            start++;
        }
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
            //print(single_char);
            retString += single_char;
        }

        return retString;
    }

    public static void print(int i){
        System.out.println(""+i);
    }

    public static void print(String str){
        System.out.println(str);
    }

    public static int randomInRange(int lowest, int highest){
        return new Random().nextInt( highest - lowest + 1) + lowest;
    }

}
