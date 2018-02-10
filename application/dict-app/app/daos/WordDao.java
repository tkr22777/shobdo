package daos;

import objects.UserRequest;
import objects.Word;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public interface WordDao {

    public Word createWord(Word word);

    public Word getWordByWordId(String wordId);

    public Word getWordBySpelling(String spelling);

    public Word updateWord(Word word);

    public void deleteWord(String wordId);

    public Set<String> getWordSpellingsWithPrefixMatch(String wordSpelling, int limit); //returns the spelling of the words that matches

    public long totalWordCount();

    public void deleteAllWords();

    public ArrayList<Word> listWords(String startWordId, int limit);

    //todo create separate request dao
    public UserRequest createRequest(UserRequest request);

    public UserRequest getRequestById(String requestId);

    public UserRequest updateRequest(UserRequest request);

    public void deleteRequest(String requestId);
}
