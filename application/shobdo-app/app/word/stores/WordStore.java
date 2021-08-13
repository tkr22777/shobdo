package word.stores;

import word.objects.Word;

import java.util.ArrayList;
import java.util.Set;

public interface WordStore {

     Word create(Word word);

     Word getById(String wordId);

     Word getBySpelling(String spelling);

     Word update(Word word);

     void delete(String wordId);

     //returns the spelling of the words that matches the query
     Set<String> searchSpellingsBySpelling(String spellingQuery, int limit);

     long count();

     void deleteAll();

     ArrayList<Word> list(String startWordId, int limit);
}
