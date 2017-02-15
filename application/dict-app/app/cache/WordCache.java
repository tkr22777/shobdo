package cache;

import objects.DictionaryWord;
import redis.clients.jedis.Jedis;
import utilities.BenchmarkLogger;
import utilities.JsonUtil;
import utilities.LogPrint;
import utilities.RedisUtil;

import java.util.Arrays;
import java.util.Set;

public class WordCache {

    private static boolean USE_REDIS = true;
    private static String DEFAULT_REDIS_HOSTNAME = "redis";

    private Jedis jedis;

    /* Redis key words */
    private final String SERACH_WORD_BY_SPELLING_PFX = "SWBS_";
    private final String GET_WORD_BY_SPELLING_PFX = "GWBS_";

    /*Redis expire time*/
    private boolean USE_REDIS_EXPIRATION_TIME = true;
    private final int REDIS_EXPIRE_TIME = 60 * 60 * 6; //in seconds

    private BenchmarkLogger bmLog = new BenchmarkLogger(WordCache.class);
    private LogPrint log = new LogPrint(WordCache.class);

    public WordCache() {

        jedis = getJedis(getHostname());
    }

    public String getHostname() {

        //DEFAULT_REDIS_HOSTNAME = "172.17.0.1";
        //DEFAULT_REDIS_HOSTNAME = "localhost";
        log.info("@WC001 Connect to redis [host:" +  DEFAULT_REDIS_HOSTNAME + "][port:6379]." );
        return DEFAULT_REDIS_HOSTNAME;
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

    public DictionaryWord getDictionaryWordBySpellingFromCache(String spelling) {

        if( !USE_REDIS || jedis == null || spelling == null )
            return null;

        bmLog.start();
        String key = getKeyForSpelling(spelling);
        String wordJsonString = jedis.get(key);

        if (wordJsonString != null) {

            DictionaryWord wordFound = (DictionaryWord) JsonUtil.toObjectFromJsonString(wordJsonString, DictionaryWord.class);
            bmLog.end("@WC003 Word [" + spelling + "] found in cache and returning");
            return wordFound;

        } else {

            bmLog.end("@WC003 Word [" + spelling + "] not found in cache.");
            return null;
        }
    }

    public void cacheDictionaryWord(DictionaryWord word) {

        if(!USE_REDIS || jedis == null || word == null)
            return;

        String key = getKeyForSpelling(word.getWordSpelling());

        try {

            bmLog.start();
            jedis.set(key, word.toJsonString());
            bmLog.end("@WC004 Word [" + word.getWordSpelling() + "] storing in cache.");

            if (USE_REDIS_EXPIRATION_TIME)
                jedis.expire(key, REDIS_EXPIRE_TIME);

        } catch (Exception ex) {

            log.info("@WC007 Error while storing JSON string of word");
        }
    }

    public Set<String> getSearchWordsBySpellingFromCache(String spelling){

        if ( !USE_REDIS || jedis == null || spelling == null)
            return null;

        bmLog.start();
        String key = getKeyForSearch(spelling);
        Set<String> words = jedis.smembers(key);

        if( words != null && words.size() > 0) {

            bmLog.end("@WC005 Search result found and returning from cache. Count: " + words.size() + ".");
            return words;
        }

        return null;
    }

    public void cacheSearchWordsBySpelling(String spelling, Set<String> words){

        if( !USE_REDIS || jedis == null || spelling == null)
            return;

        bmLog.start();
        String key = getKeyForSearch(spelling);

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

    public String getKeyForSearch(String spelling) {
        return RedisUtil.buildRedisKey( Arrays.asList( SERACH_WORD_BY_SPELLING_PFX, spelling));
    }
}
