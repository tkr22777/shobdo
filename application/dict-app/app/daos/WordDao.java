package daos;

import objects.Word;

import java.util.Set;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public interface WordDao {

    public String setWord(Word word);

    public Word getWordByWordId(String wordId);

    public Word getWordBySpelling(String spelling);

    public Set<String> getWordSpellingsWithPrefixMatch(String wordSpelling, int limit); //returns the spelling of the words that matches

    public long totalWordCount();

    public void deleteAllWords();

}
