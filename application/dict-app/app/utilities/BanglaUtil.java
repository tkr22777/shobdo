package utilities;

import java.lang.Integer;
import java.lang.String;

public final class BanglaUtil {

    private static final String START_HEX = "995"; //ক
    private static final String END_HEX = "9A8";   //ন

    private BanglaUtil() {}

    public static String generateRandomSentence(final int numberOfWords) {
        final StringBuilder sentenceBuilder = new StringBuilder();
        for (int i = 0 ; i < numberOfWords ; i++) {
            final int number = DictUtil.randomIntInRange(1, 12);
            final String word = generateRandomWord(number);
            sentenceBuilder.append(word);
            if (i < numberOfWords - 1) {
                sentenceBuilder.append(" ");
            }
        }
        return sentenceBuilder.toString();
    }

    public static String generateRandomWord(final int length) {
        final int start = Integer.parseInt(START_HEX, 16);
        final int end = Integer.parseInt(END_HEX, 16);
        final StringBuilder banglaString = new StringBuilder();

        for (int i = 0 ; i < length ; i++) {
            final int number = DictUtil.randomIntInRange(start, end);
            final char c = (char) number;
            final String single_char = "" + c;
            banglaString.append(single_char);
        }
        return banglaString.toString();
    }

    public static String generateSentenceWithWord(final String word) {
        final String preSentence = generateRandomSentence(DictUtil.randomIntInRange(2, 6));
        final String postSentence = generateRandomSentence(DictUtil.randomIntInRange(2, 4));
        return preSentence + " " + word + " " + postSentence;
    }
}
