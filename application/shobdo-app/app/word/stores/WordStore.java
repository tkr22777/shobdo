package word.stores;

import word.objects.Inflection;
import word.objects.InflectionIndex;
import word.objects.Word;

import java.util.ArrayList;
import java.util.List;

public interface WordStore {
     /* returns words (id + spelling only) matching the query prefix */
     List<Word> searchWords(String spellingQuery, int limit);

     /* CRUDL */
     Word create(Word word);
     Word getById(String wordId);
     Word getBySpelling(String spelling);
     Word getRandomWord();
     Word getWordAtIndex(int index);
     Word update(Word word);
     void delete(String wordId);
     ArrayList<Word> list(String startWordId, int limit);

     List<Word> getRandomWords(int count);

     /* Inflections */
     InflectionIndex findInflectionBySpelling(String spelling);
     InflectionIndex createInflectionIndex(InflectionIndex entry);
     void addInflectionsToWord(String wordId, List<Inflection> inflections);

     /* For test/admin purpose */
     long count();
     void deleteAll();
     void deleteAllInflectionIndexEntries();
}
