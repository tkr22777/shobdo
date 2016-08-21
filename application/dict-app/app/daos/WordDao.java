package daos;

import objects.DictionaryWord;
import sun.security.util.DisabledAlgorithmConstraints;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public interface WordDao {


    public String getDictWord(String wordName);

    public String setDictWord(String wordName, String Meaning);

    public String setDictionaryWord(DictionaryWord dictionaryWord);

    public DictionaryWord getDictionaryWord( String wordId, String wordSpeelling);

}
