package importer.gsheet;

import word.WordLogic;
import word.objects.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleSheetsImporter {

    /*
     1. Leaving the work as work in progress for the moment. I found few issues with excel sheets: a. permission issue b. data-entry incomplete.
     2. The next step is to figure out if there was any mistake in providing the links
     3. We should moved the excel data to adopt with the service data. The service data might require some adoption.
        a. Meanings should have synonyms
        b. Words should not have synonyms
     4. We should enable UI based update/deletion of words or should it be github based?
     */
    private static WordLogic wordLogic;

    public static void main(String[] args) {
        String[] sheetIds = {};

        /*
        List<Word> words = sheetIds.stream()
            .map(GoogleSheetsImporter::getWords)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        List<Word> createdWords = writeToDB(words);
        */
    }

    public static List<Word> writeToDB(List<Word> words) {
        List<Word> createdWords = words.stream()
            .map(word -> { return wordLogic.createWord(word); }
            ).collect(Collectors.toList());
        return createdWords;
    }

    public static List<Word> getWords(String googleSheetID) {
        return new ArrayList<>();
    }
}
