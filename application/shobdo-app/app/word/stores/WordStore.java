package word.stores;

import word.objects.Word;

import java.util.ArrayList;
import java.util.Set;

public interface WordStore {
     /* returns the spelling of the words that matches the query */
     Set<String> searchSpellingsBySpelling(String spellingQuery, int limit);

     /* CRUDL */
     Word create(Word word);
     Word getById(String wordId);
     Word getBySpelling(String spelling);
     Word getRandomWord();
     Word update(Word word);
     void delete(String wordId);
     ArrayList<Word> list(String startWordId, int limit);

     /* For test/admin purpose */
     long count();
     void deleteAll();
}
