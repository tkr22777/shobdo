package daos;

import objects.Word;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public interface WordDao {


    public String getDictWord(String wordName);

    public String setDictWord(String wordName, String Meaning);

}
