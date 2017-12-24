package daos;

import objects.Meaning;
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

    public boolean updateWord(Word word);

    public void deleteWord(String wordId);

    public Set<String> getWordSpellingsWithPrefixMatch(String wordSpelling, int limit); //returns the spelling of the words that matches

    public long totalWordCount();

    public void deleteAllWords();

    public ArrayList<Word> listWords(String startWordId, int limit);
}
