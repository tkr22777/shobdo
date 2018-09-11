package daos;

import objects.UserRequest;
import objects.Word;

import java.util.ArrayList;
import java.util.Set;

public interface WordDao {

     Word createWord(Word word);

     Word getWordByWordId(String wordId);

     Word getWordBySpelling(String spelling);

     Word updateWord(Word word);

     void deleteWord(String wordId);

     Set<String> searchWordSpellingsWithPrefixMatch(String wordSpelling, int limit); //returns the spelling of the words that matches

     long totalWordCount();

     void deleteAllWords();

     ArrayList<Word> listWords(String startWordId, int limit);

     //TODO create separate request dao
     UserRequest createRequest(UserRequest request);

     UserRequest getRequestById(String requestId);

     UserRequest updateRequest(UserRequest request);

     void deleteRequest(String requestId);
}
