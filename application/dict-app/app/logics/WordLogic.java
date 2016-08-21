package logics;

import daoImplementation.WordDaoImpl;
import daos.WordDao;
import objects.BaseWord;
import objects.DictionaryWord;
import utilities.LogPrint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordLogic {

    WordDao wordDaoMongo = new WordDaoImpl();
    LogPrint log = new LogPrint(WordLogic.class);

    public BaseWord getDictWord(String wordName){

        return new BaseWord( wordDaoMongo.getDictWord(wordName), wordName);

    }

    public BaseWord setDictWord(String wordName, String Meaning){

        return new BaseWord( wordDaoMongo.setDictWord(wordName,Meaning), wordName);

    }

    public DictionaryWord retriveDictionaryWord( String wordSpelling, String wordId, String arrangement){

        return null;
    }

    public void saveDictionaryWord( DictionaryWord dictionaryWord ) {

        verifyDictionaryWord(dictionaryWord);
        //wordDaoMongo
     }

    public List<String> searchWordSpellingByString(String searchString, int limit){

        //all that logic :D

        //Cache all the spellings together for search greatness!!
        //Check if there is are ways to search by string on the indexed string
        //It sould be very basic

        return new ArrayList<>();

    }

    protected void verifyDictionaryWord(DictionaryWord dictionaryWord){

        if( dictionaryWord == null)

            log.info("Dictionary Word is null.");

        else if(dictionaryWord.getMeaningForPartsOfSpeeches() != null)

            log.info("Dictionary Word Id:" + dictionaryWord.getWordId() + " meanings array is null.");

        else if( dictionaryWord.getMeaningForPartsOfSpeeches().size() == 0 )

            log.info("Dictionary Word Id:" + dictionaryWord.getWordId() + " meanings size is zero(0).");

    }


    //The following are for future feature

    public void reArrangeBy(DictionaryWord dictionaryWord, String arrangement){

        if(isFoundOnCache( dictionaryWord.getWordId(), arrangement)) {

            getFromCache(dictionaryWord.getWordId(), arrangement);
            return; //from cache

        } else {

            reArrange_(arrangement);
            storeOnCache( dictionaryWord, arrangement);
            return;
        }

    }


    private DictionaryWord getFromCache(String wordId, String arrangement){

        return null;
    }

    private void reArrange_(String arrangement){

    }

    private void storeOnCache(DictionaryWord dictionaryWord, String arrangement){

    }

    public boolean isFoundOnCache(String wordId, String arrangement){

        return false;
    }
}
