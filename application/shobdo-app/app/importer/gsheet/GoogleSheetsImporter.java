package importer.gsheet;

import common.stores.MongoStoreFactory;
import exceptions.EntityDoesNotExist;
import word.WordLogic;
import word.caches.WordCache;
import word.objects.Meaning;
import word.objects.Word;
import word.stores.WordStoreMongoImpl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

import static importer.gsheet.SheetsQuickstart.getWordsWithMeaning;

/* this is no prod code, this is only for initial import */
public class GoogleSheetsImporter {

    private static WordLogic wordLogic;

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        String[] sheetIds = {
            // google-sheet ids
        };

        List<Word> words = getWords(Arrays.asList(sheetIds));
        System.out.println("Total valid words found:" + words.size());

        List<Meaning> meanings = words.stream()
            .map( w -> {
                Map<String, Meaning> meaningMap = w.getMeanings();
                if (meaningMap == null) {
                    meaningMap = new HashMap<>();
                }
                return meaningMap.values().stream().collect(Collectors.toList());
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        System.out.println("Total valid meanings found:" + meanings.size());

        WordStoreMongoImpl storeMongoDB = new WordStoreMongoImpl(MongoStoreFactory.getWordCollection());
        wordLogic = new WordLogic(storeMongoDB, WordCache.getCache());

        for (Word word: words) {
            if (word.getSpelling() == null || "".equalsIgnoreCase(word.getSpelling().trim())) {
                continue;
            }
            Word newWord = Word.builder()
                .spelling(word.getSpelling())
                .build();

            newWord = wordLogic.createWord(newWord);

            Map<String, Meaning> meaningMap = word.getMeanings();
            if (meaningMap != null) {
                for (Map.Entry<String, Meaning> entry: meaningMap.entrySet()) {
                    Meaning wordMeaning = entry.getValue();
                    Meaning toAdd = Meaning.fromMeaning(wordMeaning);
                    if (toAdd.getText() == null || "".equalsIgnoreCase(toAdd.getText().trim())) {
                        continue;
                    }
                    toAdd.setId(null);
                    wordLogic.createMeaning(newWord.getId(), toAdd);
                }
            }
        }
    }

    private static List<Word> getWords(List<String> sheetIds) throws IOException, GeneralSecurityException {
        Map<String, Word> wordMap = new HashMap<>();
        for (String spreadsheetId: sheetIds) {
            Map<String, Word> wordMapForSheet = getWordsWithMeaning(spreadsheetId);
            for (Map.Entry<String, Word> e: wordMapForSheet.entrySet()) {
                if (wordMap.containsKey(e.getKey())) {
                    Word word = wordMap.get(e.getKey());
                    Word mergedWord = mergeWords(word, e.getValue());
                    wordMap.put(mergedWord.getSpelling(), mergedWord);
                } else {
                    wordMap.put(e.getKey(), e.getValue());
                }
            }
        }

        return new ArrayList<>(wordMap.values());
    }

    private static Word mergeWords(Word w1, Word w2) throws EntityDoesNotExist {
        HashMap<String, Meaning> w1MeaningsMap = w1.getMeanings() != null? w1.getMeanings(): new HashMap<>();
        HashMap<String, Meaning> w2MeaningsMap = w2.getMeanings() != null? w2.getMeanings(): new HashMap<>();
        w1MeaningsMap.putAll(w2MeaningsMap);
        return Word.builder()
            .spelling(w1.getSpelling())
            .meanings(w1MeaningsMap)
            .build();
    }
}
