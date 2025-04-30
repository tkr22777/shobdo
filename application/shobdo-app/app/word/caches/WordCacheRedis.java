package word.caches;

import com.typesafe.config.ConfigFactory;
import redis.clients.jedis.Jedis;
import utilities.*;
import word.objects.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class WordCacheRedis {

    /* Redis expire time */
    private static final boolean USE_REDIS_EXPIRATION_TIME = true;
    private static final int REDIS_EXPIRE_TIME_SECONDS= 6 * 60 * 60; //six hours

    private static final Logger log = LoggerFactory.getLogger(WordCacheRedis.class);

    /* the following are singletons */
    private static Jedis jedis;
    private static WordCacheRedis instance;

    private WordCacheRedis() {
    }

    public synchronized static WordCacheRedis getCache() {
        if (jedis == null) {
            final String DEFAULT_REDIS_HOSTNAME = ConfigFactory.load().getString("shobdo.redis.hostname");
            try {
                log.info("@WC001 Connecting to redis [host:" + DEFAULT_REDIS_HOSTNAME + "][port:6379].");
                jedis = new Jedis(DEFAULT_REDIS_HOSTNAME);
            } catch (Exception ex) {
                log.error("@WC002 Error while connecting to redis [host:" + DEFAULT_REDIS_HOSTNAME + "][port:6379].", ex);
            }
        }

        if (instance == null) {
            instance = new WordCacheRedis();
        }

        return instance;
    }

    public Word getBySpelling(final String spelling) {
        if (spelling == null || jedis == null) {
            return null;
        }

        try {
            final String wordJsonString = jedis.get(getKeyForSpelling(spelling));
            if (wordJsonString != null) {
                log.debug("@WC003 Word [" + spelling + "] found in cache and returning");
                return (Word) JsonUtil.jStringToObject(wordJsonString, Word.class);
            }
        } catch (Exception ex) {
            log.error("@WC005 Error while getting word by spelling from cache", ex);
        }
        log.error("@WC005 Could not find word by spelling in cache");
        return null;
    }

    public void cacheBySpelling(final Word word) {
        if (word == null || jedis == null) {
            return;
        }

        final String key = getKeyForSpelling(word.getSpelling());
        try {
            jedis.set(key, word.toString());
            if (USE_REDIS_EXPIRATION_TIME) {
                jedis.expire(key, REDIS_EXPIRE_TIME_SECONDS);
            }
            log.debug("@WC004 Word [" + word.getSpelling() + "] stored in cache.");
        } catch (Exception ex) {
            log.error("@WC005 Error while caching word by spelling", ex);
        }
    }

    public void invalidateBySpelling(final Word word) {
        if (word == null || jedis == null) {
            return;
        }

        try {
            jedis.del(getKeyForSpelling(word.getSpelling()));
            log.debug("@WC006 Word [" + word.getSpelling() + "] cleared from cache.");
        } catch (Exception ex) {
            log.error("@WC007 Error while invalidating cached word", ex);
        }
    }

    public Set<String> getWordsForSearchString(final String searchString){
        if (searchString == null || jedis == null) {
            return null;
        }

        try {
            final String result = jedis.get(getKeyForSearchString(searchString));
            if (result != null) {
                log.debug("@WC008 Search result found and returning from cache.");
                return  (Set<String>) JsonUtil.jStringToObject(result, Set.class);
            }
        } catch (Exception ex) {
            log.error("@WC010 Error while retrieving cached search results", ex);
        }

        log.debug("@WC009 Search result not found on cache for spelling: '" + searchString + "'");
        return null;
    }

    public void cacheWordsForSearchString(final String searchString, final Set<String> spellings) {
        if (searchString == null || spellings == null || jedis == null) {
            return;
        }

        final String key = getKeyForSearchString(searchString);
        try {
            jedis.set(key, JsonUtil.objectToJString(spellings));
            if (USE_REDIS_EXPIRATION_TIME) {
                jedis.expire(key, REDIS_EXPIRE_TIME_SECONDS);
            }
            log.info("@WC011 Storing search results on cache. Count: " + spellings.size() + ".");
        } catch (Exception ex) {
            log.error("@WC012 Error while caching search results", ex);
        }
    }

    private String getKeyForSpelling(String spelling) {
        return String.format("SP_%s", spelling);
    }

    private String getKeyForSearchString(String searchString) {
        return String.format("SS_%s", searchString);
    }

    public void flushCache(){
        if (jedis != null) {
            try {
                jedis.flushAll();
            } catch (Exception ex) {
                log.error("@WC013 Error while flushing cache", ex);
            }
        }
    }
}
