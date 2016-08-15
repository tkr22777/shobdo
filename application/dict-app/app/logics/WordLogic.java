package logics;

import objects.Word;
import play.mvc.Result;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordLogic {

    public Word getDictWord(String wordName){

        return new Word( wordName + "ID", wordName);

    }

}
