package logics;

import daoImplementation.WordDaoImpl;
import daos.WordDao;
import objects.Word;
import play.mvc.Result;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordLogic {

    WordDao wordDaoMongo = new WordDaoImpl();

    public Word getDictWord(String wordName){

        return new Word( wordDaoMongo.getDictWord(wordName), wordName);

    }

    public Word setDictWord(String wordName, String Meaning){

        return new Word( wordDaoMongo.setDictWord(wordName,Meaning), wordName);

    }

}
