package word.stores;

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

     /* For test/admin purpose */
     long count();
     void deleteAll();
}
