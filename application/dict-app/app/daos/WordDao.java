package daos;

import objects.UserRequest;
import objects.Word;

import java.util.ArrayList;
import java.util.Set;

public interface WordDao {

     Word create(Word word);

     Word getById(String wordId);

     Word getBySpelling(String spelling);

     Word update(Word word);

     void delete(String wordId);

     //returns the spelling of the words that matches the query
     Set<String> searchSpellingsBySpelling(String spellingQuery, int limit);

     long totalCount();

     void deleteAll();

     ArrayList<Word> list(String startWordId, int limit);

     //TODO create separate request dao
     UserRequest createUserRequest(UserRequest request);

     UserRequest getUserRequest(String requestId);

     UserRequest updateUserRequest(UserRequest request);

     void deleteUserRequest(String requestId);
}
