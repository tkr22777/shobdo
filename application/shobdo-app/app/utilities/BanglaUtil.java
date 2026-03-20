package utilities;

import java.lang.Integer;
import java.lang.String;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class BanglaUtil {

    // Matches any character from a foreign script that must not appear in Bengali spellings.
    // Bengali characters, Latin letters, digits, and common punctuation are all permitted.
    private static final Pattern FOREIGN_SCRIPT_PATTERN = Pattern.compile(
        "[\\p{InDevanagari}\\p{InArabic}\\p{InArabic_Supplement}" +
        "\\p{InKannada}\\p{InGurmukhi}\\p{InTamil}\\p{InTelugu}" +
        "\\p{InMalayalam}\\p{InOriya}\\p{InSinhala}\\p{InTibetan}" +
        "\\p{InMyanmar}\\p{InHangul_Syllables}\\p{InHiragana}" +
        "\\p{InKatakana}\\p{InThai}\\p{InHebrew}\\p{InCyrillic}" +
        "\\p{InGreek}]"
    );

    /**
     * Returns {@code true} if {@code spelling} contains any character from a
     * foreign script (Devanagari, Arabic, Kannada, Gurmukhi, etc.).
     * Bengali characters, Latin letters, digits, and common punctuation are
     * all considered acceptable and will not trigger a {@code true} result.
     */
    public static boolean containsForeignScript(final String spelling) {
        if (spelling == null) return false;
        return FOREIGN_SCRIPT_PATTERN.matcher(spelling).find();
    }

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

    /* package private */ static Set<String> generateRandomWordStringSet(final int size, final int maxWordLength) {
        Set<String> words = new HashSet<>();
        for (int i = 0; i < size; i++) {
            words.add(generateRandomWordString(maxWordLength));
        }
        return words;
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
