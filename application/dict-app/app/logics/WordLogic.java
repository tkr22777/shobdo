package logics;

import cache.WordCache;
import daoImplementation.WordDaoMongoImpl;
import daos.WordDao;
import objects.DictionaryWord;
import utilities.BenchmarkLogger;
import utilities.Constants;
import utilities.LogPrint;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordLogic {

    private WordDao wordDao;
    private WordCache wordCache;

    private BenchmarkLogger bmLog = new BenchmarkLogger(WordLogic.class);
    private LogPrint log = new LogPrint(WordLogic.class);

    public static WordLogic factory() { //to select which database to use

        WordDao wordDao = new WordDaoMongoImpl();
        return new WordLogic( wordDao, new WordCache() );
    }

    public WordLogic( WordDao wordDao, WordCache wordCache) {

        this.wordDao = wordDao;
        this.wordCache = wordCache;
    }

    public void saveDictionaryWord( DictionaryWord dictionaryWord ) {

        wordDao.setDictionaryWord(dictionaryWord);
        wordCache.cacheDictionaryWord(dictionaryWord);
    }

    public void saveDictionaryWords(Collection<DictionaryWord> dictionaryWords ) {

        for(DictionaryWord word: dictionaryWords) {
            wordDao.setDictionaryWord(word);
            wordCache.cacheDictionaryWord(word);
        }
    }

    public DictionaryWord getDictionaryWordBySpelling( String spelling ){

        if(spelling == null || spelling == "")
            throw new IllegalArgumentException("WLEX: getDictionaryWordBySpelling word spelling is null or empty");

        DictionaryWord cachedWord = wordCache.getDictionaryWordBySpellingFromCache(spelling);

        if(cachedWord != null)
            return cachedWord;

        DictionaryWord wordFromDB = wordDao.getDictionaryWordBySpelling(spelling);

        wordCache.cacheDictionaryWord(wordFromDB);

        return wordFromDB;

    }

    public DictionaryWord getDictionaryWordByWordId( String wordId) {

        if(wordId == null)
            throw new IllegalArgumentException("WLEX: getDictionaryWordByWordId wordId is null or empty");

        bmLog.start();
        DictionaryWord word = wordDao.getDictionaryWordByWordId(wordId);
        bmLog.end("@WL001 Word [ID:" + wordId + "][Spelling"+ word.getWordSpelling() +"] found in database and returning");

        return word;
    }

    /**
     Cache all the spellings together for search!!
     Check if there is are ways to search by string on the indexed string, it should be very basic!
     ** You may return a smart object that specifies each close words and also suggestion if it didn't match
     How to find closest neighbour of a Bangla word? you may be able to do that locally?
     **/
    public Set<String> searchWordsBySpelling(String spelling) {
        return searchWordsBySpelling(spelling, Constants.SEARCH_SPELLING_LIMIT);
    }

    public Set<String> searchWordsBySpelling(String spelling, int limit){

        if(spelling == null || spelling.equals(""))
            throw new IllegalArgumentException("WLEX: searchWordsBySpelling spelling is null or empty");

        Set<String> words = wordCache.getSearchWordsBySpellingFromCache(spelling);

        if(words != null && words.size() > 0)
            return words;

        bmLog.start();
        words = wordDao.getWordSpellingsWithPrefixMatch(spelling, limit);

        if ( words != null && words.size() > 0 ) {

            bmLog.end("@WL003 search result [size:" + words.size() + "] for spelling:\"" + spelling + "\" found in database and returning");
            wordCache.cacheSearchWordsBySpelling(spelling, words);
            return words;

        } else {

            bmLog.end("@WL003 search result for spelling:\"" + spelling + "\" not found in database");
            return new HashSet<>();
        }
    }

    public long totalWordCount(){
        return wordDao.totalWordCount();
    }

    public void deleteAllWords(){
        wordDao.deleteAllWords();
    }

    public void flushCache(){
        wordCache.flushCache();
    }

    protected void verifyDictionaryWord(DictionaryWord dictionaryWord) {

        if( dictionaryWord == null)

            log.info("Dictionary Word is null.");

        else if(dictionaryWord.getMeaningForPartsOfSpeeches() == null)

            log.info("Dictionary Word Id:" + dictionaryWord.getWordId() + " meanings array is null.");

        else if( dictionaryWord.getMeaningForPartsOfSpeeches().size() == 0 )

            log.info("Dictionary Word Id:" + dictionaryWord.getWordId() + " meanings size is zero(0).");

    }

    public static DictionaryWord copyToNewDictWordObject(DictionaryWord providedWord) {

        DictionaryWord toReturnWord = new DictionaryWord();

        toReturnWord.setWordId( Constants.WORD_ID_PREFIX + UUID.randomUUID() );

        if(providedWord != null) {

            if(providedWord.getArrangementType() != null)
                toReturnWord.setArrangementType( new String(providedWord.getArrangementType()) );

            toReturnWord.setVersion( providedWord.getVersion() + 1 );

            if(providedWord.getWordSpelling() != null)
                toReturnWord.setWordSpelling(providedWord.getWordSpelling());

            if(providedWord.getOtherSpellings() != null)
                toReturnWord.setOtherSpellings(providedWord.getOtherSpellings());

            toReturnWord.setTimesSearched(providedWord.getTimesSearched());

            if(providedWord.getLinkToPronunciation() != null)
                toReturnWord.setLinkToPronunciation(providedWord.getLinkToPronunciation());

            if(providedWord.getExtraMetaMap() != null)
                toReturnWord.setExtraMetaMap( providedWord.getExtraMetaMap() );
        }

        return toReturnWord;
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
