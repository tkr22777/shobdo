package daos;

import objects.DictionaryWord;
import sun.security.util.DisabledAlgorithmConstraints;

import java.util.ArrayList;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public interface WordDao {


    public String getDictWord(String wordName);

    public String setDictWord(String wordName, String Meaning);

    public String setDictionaryWord(DictionaryWord dictionaryWord);

    public DictionaryWord getDictionaryWordByWordId(String wordId);

    public DictionaryWord getDictionaryWordBySpelling(String spelling);

    public ArrayList<String> getWordsWithPrefixMatch(String wordSpelling); //returns the spelling of the words that matches

}
