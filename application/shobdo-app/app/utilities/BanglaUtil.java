package utilities;

import java.lang.Integer;
import java.lang.String;

public final class BanglaUtil {

    private static final String START_HEX = "995"; //ক
    private static final String END_HEX = "9A8";   //ন

    private BanglaUtil() {}

    /* package private */ static String generateRandomSentence(final int numberOfWords) {
        final StringBuilder sentenceBuilder = new StringBuilder();
        for (int i = 0 ; i < numberOfWords ; i++) {
            final int number = DictUtil.randIntInRange(1, 12);
            final String word = generateRandomWordString(number);
            sentenceBuilder.append(word);
            if (i < numberOfWords - 1) {
                sentenceBuilder.append(" ");
            }
        }
        return sentenceBuilder.toString();
    }

    /* package private */ static String generateRandomWordString(final int length) {
        final int start = Integer.parseInt(START_HEX, 16);
        final int end = Integer.parseInt(END_HEX, 16);
        final StringBuilder banglaString = new StringBuilder();

        for (int i = 0 ; i < length ; i++) {
            final int number = DictUtil.randIntInRange(start, end);
            final char c = (char) number;
            final String single_char = "" + c;
            banglaString.append(single_char);
        }
        return banglaString.toString();
    }

    /* package private */ static String generateSentenceWithWord(final String word) {
        final String preSentence = generateRandomSentence(DictUtil.randIntInRange(2, 6));
        final String postSentence = generateRandomSentence(DictUtil.randIntInRange(2, 4));
        return preSentence + " " + word + " " + postSentence;
    }
}
