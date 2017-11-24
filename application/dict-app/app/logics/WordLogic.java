package logics;

import cache.WordCache;
import daoImplementation.WordDaoMongoImpl;
import daos.WordDao;
import objects.Word;
import utilities.BenchmarkLogger;
import utilities.Constants;
import utilities.DictUtil;
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

    public static WordLogic factory() {

        WordDao wordDao = new WordDaoMongoImpl();
        return new WordLogic( wordDao, new WordCache() );
    }

    public WordLogic( WordDao wordDao, WordCache wordCache) {

        this.wordDao = wordDao;
        this.wordCache = wordCache;
    }

    /* We will version each updates/deletes:

        For an update:
            We will copy the current object, copyMeaning
            On copyMeaning,
                set a new meaningId, //since we do not want to destruct the existingId
                set a deletedDate,
                set an event note, //Update
            On the actual object,
                update data,
                set the parentMeaningId to the meaningId for the copyMeaning

        For a delete:
            Set the deletedDate
            Set an event note, //Delete
    */

    //todo
    public void updateWord(Word word) {

    }

    public void saveWord(Word word) {

        wordDao.saveWord(word);
        wordCache.cacheWord(word);
    }

    public void saveWords(Collection<Word> words) {

        for(Word word: words) {
            wordDao.saveWord(word);
            wordCache.cacheWord(word);
        }
    }

    public Word getWordBySpelling(String spelling ){

        if(spelling == null || spelling == "")
            throw new IllegalArgumentException("WLEX: getWordBySpelling word spelling is null or empty");

        Word cachedWord = wordCache.getWordBySpelling(spelling);

        if(cachedWord != null)
            return cachedWord;

        Word wordFromDB = wordDao.getWordBySpelling(spelling);

        wordCache.cacheWord(wordFromDB);

        return wordFromDB;

    }

    public Word getWordByWordId(String wordId) {

        if(wordId == null)
            throw new IllegalArgumentException("WLEX: getWordByWordId wordId is null or empty");

        bmLog.start();
        Word word = wordDao.getWordByWordId(wordId);
        bmLog.end("@WL001 Word [ID:" + wordId + "][Spelling"+ word.getWordSpelling() +"] found in database and returning");

        return word;
    }

    public void updateWord(String wordId, Word word) {

        if(wordId == null)
            throw new IllegalArgumentException("wordId is null, cannot update");

        if(word.getWordId() == null)
            word.setWordId(wordId);

        if(word.getWordId().equals(wordId))
            throw new IllegalArgumentException("wordId on route do not match");

        updateWord(word);
    }

    public void updateWord(Word word) {

        verifyWordForUpsert(word);

    }

    /**
     Cache all the spellings together for search!!
     Check if there is are ways to search by string on the indexed string, it should be very basic!
     ** You may return a smart object that specifies each close words and also suggestion if it didn't match
     How to find closest neighbour of a BanglaUtil word? you may be able to do that locally?
     **/
    public Set<String> searchWords(String searchSting) {
        return searchWords(searchSting, Constants.SEARCH_SPELLING_LIMIT);
    }

    public Set<String> searchWords(String searchString, int limit){

        if(searchString == null || searchString.equals(""))
            throw new IllegalArgumentException("WLEX: searchWords spelling is null or empty");

        Set<String> words = wordCache.getWordsForSearchString(searchString);

        if(words != null && words.size() > 0)
            return words;

        bmLog.start();
        words = wordDao.getWordSpellingsWithPrefixMatch(searchString, limit);

        if ( words != null && words.size() > 0 ) {

            bmLog.end("@WL002 search result [size:" + words.size() + "] for spelling:\"" + searchString + "\" found in database and returning");
            wordCache.cacheWordsForSearchString(searchString, words);
            return words;

        } else {

            bmLog.end("@WL003 search result for spelling:\"" + searchString + "\" not found in database");
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

    public static Word copyToNewWordObject(Word providedWord) {

        Word toReturnWord = new Word();

        toReturnWord.setWordId(DictUtil.generateNewWordId());

        if(providedWord != null) {

            toReturnWord.setVersion( providedWord.getVersion() + 1 );

            if(providedWord.getWordSpelling() != null)
                toReturnWord.setWordSpelling(providedWord.getWordSpelling());

            if(providedWord.getExtraMetaMap() != null)
                toReturnWord.setExtraMetaMap( providedWord.getExtraMetaMap() );
        }

        return toReturnWord;
    }

    private void verifyWordForUpsert(Word word) {

        if(word.getWordId() == null) {
            log.info("@WL004 wordId is null");
            throw new IllegalArgumentException("wordId is null");
        }

        if(word.getWordSpelling() == null || word.getWordSpelling().trim().length() == 0) {
            log.info("@WL005 wordSpelling is null or empty");
            throw new IllegalArgumentException("word spelling cannot be null or empty");
        }

        if(word.getMeanings() == null || word.getMeanings().size() == 0) {
            log.info("@WL006 meanings array is null or empty");
            throw new IllegalArgumentException("word meaning cannot be null or empty");
        }
    }

}
