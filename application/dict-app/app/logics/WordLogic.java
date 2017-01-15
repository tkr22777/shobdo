package logics;

import cache.WordCache;
import daoImplementation.WordDaoMongoImpl;
import daos.WordDao;
import objects.DictionaryWord;
import utilities.Constants;
import utilities.LogPrint;

import java.util.Set;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordLogic {

    private WordDao wordDao;
    private WordCache wordCache;

    private LogPrint log = new LogPrint(WordLogic.class);

    public static WordLogic factory(String dbName) { //to select which database to use

        if(dbName == null)
            dbName = Constants.DB_DEFAULT;

        WordDao wordDao;

        if(Constants.DB_MONGO.equalsIgnoreCase(dbName))
            wordDao = new WordDaoMongoImpl();
        else
            wordDao = new WordDaoMongoImpl();

        return new WordLogic( wordDao, new WordCache() );
    }

    public WordLogic( WordDao wordDao, WordCache wordCache) {

        this.wordDao = wordDao;

        this.wordCache = wordCache;
    }

    public void saveDictionaryWord( DictionaryWord dictionaryWord ) {

        verifyDictionaryWord(dictionaryWord);

        wordDao.setDictionaryWord(dictionaryWord);

        wordCache.cacheDictionaryWord(dictionaryWord);
    }

    public DictionaryWord getDictionaryWordBySpelling( String spelling ){

        if(spelling == null)
            throw new IllegalArgumentException("WLEX: getDictionaryWordBySpelling word spelling is null or empty");

        DictionaryWord word = wordCache.getDictionaryWordBySpellingFromCache(spelling);

        if( word != null )
            return word;

        word = wordDao.getDictionaryWordBySpelling(spelling);

        wordCache.cacheDictionaryWord(word);

        return word;
    }

    public DictionaryWord getDictionaryWordByWordId( String wordId) {

        if(wordId == null)
            throw new IllegalArgumentException("WLEX: getDictionaryWordByWordId wordId is null or empty");

        return wordDao.getDictionaryWordByWordId(wordId);
    }

    /**
     Cache all the spellings together for search!!
     Check if there is are ways to search by string on the indexed string, it should be very basic!
     ** You may return a smart object that specifies each close words and also suggestion if it didn't match
     How to find closest neighbour of a Bangla word? you may be able to do that locally?
     **/

    public Set<String> searchWordsBySpelling(String spelling, int limit){

        if(spelling == null)
            throw new IllegalArgumentException("WLEX: searchWordsBySpelling spelling is null or empty");

        Set<String> words = wordCache.getSearchWordsBySpellingFromCache(spelling);

        if(words != null && words.size() > 0)
            return words;

        words = wordDao.getWordSpellingsWithPrefixMatch(spelling, limit);

        if ( words != null && words.size() > 0 )
            wordCache.cacheSearchWordsBySpelling(spelling,words);

        return words;
    }

    public long totalWordCount(){
        return wordDao.totalWordCount();
    }

    protected void verifyDictionaryWord(DictionaryWord dictionaryWord){

        if( dictionaryWord == null)

            log.info("Dictionary Word is null.");

        else if(dictionaryWord.getMeaningForPartsOfSpeeches() == null)

            log.info("Dictionary Word Id:" + dictionaryWord.getWordId() + " meanings array is null.");

        else if( dictionaryWord.getMeaningForPartsOfSpeeches().size() == 0 )

            log.info("Dictionary Word Id:" + dictionaryWord.getWordId() + " meanings size is zero(0).");

    }

    //Word arrangement is a future feature

    public void reArrangeBy(DictionaryWord dictionaryWord, String arrangement){

        if(isFoundOnCache( dictionaryWord.getWordId(), arrangement)) {

            getFromCache(dictionaryWord.getWordId(), arrangement);
            return; //from cache

        } else {

            _reArrange(arrangement);
            storeOnCache( dictionaryWord, arrangement);
            return;
        }
    }


    private DictionaryWord getFromCache(String wordId, String arrangement){

        return null;
    }

    private void _reArrange(String arrangement){

    }

    private void storeOnCache(DictionaryWord dictionaryWord, String arrangement){

    }

    public boolean isFoundOnCache(String wordId, String arrangement){

        return false;
    }
}
