package logics;

import daoImplementation.WordDaoMongoImpl;
import daos.WordDao;
import objects.BaseWord;
import objects.DictionaryWord;
import org.omg.CORBA.Object;
import redis.clients.jedis.Jedis;
import utilities.LogPrint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordLogic {

    private static final String DB_MONGO = "MONGODB";
    private static final String DB_DEFAULT = DB_MONGO;

    private WordDao wordDao;

    //Is it a better idea to have a class wordCache or something?
    private static boolean USE_REDIS = true;
    private static final String REDIS_HOSTNAME="localhost";
    private Jedis jedis;

    /* Redis key words */
    private final String REDIS_SERACH_WORD_BY_SPELLING = "SRC_WD_BY_SPL";
    private final String REDIS_GET_WORD_BY_SPELLING = "GET_WD_BY_SPL";

    /*Redis expire time*/
    private boolean USE_REDIS_EXPIRATION_TIME = false;
    private final int REDIS_EXPIRE_TIME = 10; //in seconds

    private LogPrint log = new LogPrint(WordLogic.class);

    private WordLogic( WordDao wordDao, boolean useRedis) {

        this.wordDao = wordDao;

        if(useRedis){

            jedis = getJedis(REDIS_HOSTNAME);

        }

    }

    public Jedis getJedis( String hostname){

        Jedis jedis = null;

        try {

            jedis = new Jedis(hostname);

        } catch (Exception ex) {

            log.info("Exception Occured while connecting to Redis. Message:" + ex.getMessage() );
        }

        return jedis;

    }

    public static WordLogic factory(String dbName) { //to select which database to use

        if(dbName == null)
            dbName = DB_DEFAULT;

        WordDao wordDao;

        if(DB_MONGO.equalsIgnoreCase(dbName))
            wordDao = new WordDaoMongoImpl();
        else                                    //if(DB_DEFAULT.equalsIgnoreCase(dbName))
            wordDao = new WordDaoMongoImpl();   // Default

        return new WordLogic( wordDao, USE_REDIS);

    }

    public BaseWord getDictWord(String wordName){

        return new BaseWord( wordDao.getDictWord(wordName), wordName);

    }

    public BaseWord setDictWord(String wordName, String Meaning){

        return new BaseWord( wordDao.setDictWord(wordName,Meaning), wordName);

    }

    public DictionaryWord getDictionaryWordBySpelling( String spelling, String arrangement){

        if(arrangement != null)
            log.info("Arrangement not avaiable in current version");

        String key = REDIS_GET_WORD_BY_SPELLING + spelling;

        log.info("key:" + key);

        if( jedis != null ) {

            String dictionaryWord = jedis.smembers(key);

            if( word != null ) {
                log.debug("Word [" + spelling + "] found and returning from redis.");
                return new DictionaryWord(dictionaryWord);
            }
        }

        DictionaryWord word = wordDao.getDictionaryWordBySpelling(spelling);

        if ( jedis != null && word != null ) {

            jedis.add(key, word.toString());

            if(USE_REDIS_EXPIRATION_TIME)
                jedis.expire( key, REDIS_EXPIRE_TIME);
        }

        return word;
    }

    public DictionaryWord getDictionaryWordByWordId( String wordId, String arrangement) {

        if (arrangement != null)
            log.info("Arrangement not avaiable in current version");

        return wordDao.getDictionaryWordByWordId(wordId);

    }

    public void saveDictionaryWord( DictionaryWord dictionaryWord ) {

        verifyDictionaryWord(dictionaryWord);

        wordDao.setDictionaryWord(dictionaryWord);

     }

    public List<String> searchWordsBySpelling(String spelling, int limit){

        //all that logic :D
        //Cache all the spellings together for search greatness!!
        //Check if there is are ways to search by string on the indexed string
        //It sould be very basic
        //You may return a smart object that specifies each close words and also suggestion if it didn't match
        //Also how to find closest neighbour of a Bangla word?
        //you may be able to do that locally

        String key = REDIS_SERACH_WORD_BY_SPELLING + spelling;

        log.info("key:" + key);

        if( jedis != null ) {

            Set<String> words = jedis.smembers(key);

            if( words != null && words.size() > 0) {
                log.debug("Search result found and returning from redis. Count: " + words.size());
                return new ArrayList<>(words);
            }
        }

        ArrayList<String> words = wordDao.getWordsWithPrefixMatch(spelling);

        if ( jedis != null && words != null && words.size() > 0 ) {

            for(String word: words) {
                jedis.sadd(key, word);
            }


            if(USE_REDIS_EXPIRATION_TIME)
                jedis.expire( key, REDIS_EXPIRE_TIME);
        }

        return words;
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
