package cache;

import com.typesafe.config.ConfigFactory;
import objects.Word;
import redis.clients.jedis.Jedis;
import utilities.*;

import java.util.Arrays;
import java.util.Set;

public class WordCache {

    private static boolean USE_REDIS = true;

    private Jedis jedis;

    /* Redis key words */
    private final String SERACH_WORD_BY_SPELLING_PFX = "SWBS_";
    private final String GET_WORD_BY_SPELLING_PFX = "GWBS_";

    /* Redis expire time */
    private boolean USE_REDIS_EXPIRATION_TIME = true;
    private final int REDIS_EXPIRE_TIME = 60 * 60 * 6; //in seconds

    private BenchmarkLogger bmLog = new BenchmarkLogger(WordCache.class);
    private LogPrint log = new LogPrint(WordCache.class);

    public WordCache() {

        String DEFAULT_REDIS_HOSTNAME = ConfigFactory.load().getString(Constants.REDIS_HOSTNAME_CONFIG_STRING);
        log.info("@WC001 Connect to redis [host:" +  DEFAULT_REDIS_HOSTNAME + "][port:6379]." );
        jedis = getJedis(DEFAULT_REDIS_HOSTNAME);
    }

    public Jedis getJedis(String hostname) {

        Jedis jedis = null;

        try {

            jedis = new Jedis(hostname);

        } catch (Exception ex) {

            log.info("@WC002 Error connecting to Redis. Message:" + ex.getMessage());
        }

        return jedis;
    }

    public Word getWordBySpelling(String spelling) {

        if( !USE_REDIS || jedis == null || spelling == null )
            return null;

        return getWordFromRedis(spelling);
    }

    private Word getWordFromRedis(String spelling) {

        bmLog.start();
        String key = getKeyForSpelling(spelling);
        String wordJsonString = jedis.get(key);

        if (wordJsonString != null) {

            Word wordFound = (Word) JsonUtil.toObjectFromJsonString(wordJsonString, Word.class);
            bmLog.end("@WC003 Word [" + spelling + "] found in cache and returning");
            return wordFound;

        } else {

            bmLog.end("@WC003 Word [" + spelling + "] not found in cache.");
            return null;
        }
    }

    public void cacheWord(Word word) {

        if(!USE_REDIS || jedis == null || word == null)
            return;

        bmLog.start();
        String key = getKeyForSpelling(word.getWordSpelling());

        try {

            jedis.set(key, word.toString());
            bmLog.end("@WC004 Word [" + word.getWordSpelling() + "] stored in cache.");

            if (USE_REDIS_EXPIRATION_TIME)
                jedis.expire(key, REDIS_EXPIRE_TIME);

        } catch (Exception ex) {

            log.info("@WC007 Error while storing JSON string of word");
        }
    }

    public Set<String> getWordsForSearchString(String searchString){

        if ( !USE_REDIS || jedis == null || searchString == null)
            return null;

        bmLog.start();
        String key = getKeyForSearchString(searchString);
        Set<String> words = jedis.smembers(key);

        if( words != null && words.size() > 0) {

            bmLog.end("@WC005 Search result found and returning from cache. Count: " + words.size() + ".");
            return words;

        } else {

            bmLog.end("@WC005 Search result not found on cache for spelling: \'" + searchString + "\'");
            return null;
        }
    }

    public void cacheWordsForSearchString(String searchString, Set<String> words){

        if( !USE_REDIS || jedis == null || searchString == null)
            return;

        bmLog.start();
        String key = getKeyForSearchString(searchString);

        if(words == null) { //invalidate existing value
            jedis.del(key);
            return;
        }

        for(String word: words) {
            jedis.sadd(key, word);
        }

        if(USE_REDIS_EXPIRATION_TIME)
            jedis.expire( key, REDIS_EXPIRE_TIME);

        bmLog.end("@WC006 Storing search results on cache. Count: " + words.size() + ".");
    }

    public String getKeyForSpelling(String spelling) {
        return RedisUtil.buildRedisKey( Arrays.asList( GET_WORD_BY_SPELLING_PFX, spelling) );
    }

    public String getKeyForSearchString(String searchString) {
        return RedisUtil.buildRedisKey( Arrays.asList( SERACH_WORD_BY_SPELLING_PFX, searchString));
    }

    public void flushCache(){
        jedis.flushAll();
    }

}
