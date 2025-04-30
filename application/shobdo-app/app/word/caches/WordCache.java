package word.caches;

import utilities.*;
import word.objects.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WordCache {

    private static final Logger log = LoggerFactory.getLogger(WordCache.class);

    /* In-memory caches */
    private final Map<String, Word> wordsBySpelling;
    private final Map<String, Set<String>> searchResults;
    
    /* Singleton instance */
    private static WordCache instance;

    private WordCache() {
        this.wordsBySpelling = new ConcurrentHashMap<>();
        this.searchResults = new ConcurrentHashMap<>();
    }

    public synchronized static WordCache getCache() {
        if (instance == null) {
            instance = new WordCache();
            log.info("@WC001 Initialized in-memory word cache");
        }
        return instance;
    }

    public Word getBySpelling(final String spelling) {
        if (spelling == null) {
            return null;
        }

        String key = getKeyForSpelling(spelling);
        Word word = wordsBySpelling.get(key);
        
        if (word != null) {
            log.debug("@WC003 Word [" + spelling + "] found in cache and returning");
            return word;
        }
        
        log.debug("@WC005 Could not find word by spelling in cache");
        return null;
    }

    public void cacheBySpelling(final Word word) {
        if (word == null) {
            return;
        }

        final String key = getKeyForSpelling(word.getSpelling());
        wordsBySpelling.put(key, word);
        log.debug("@WC004 Word [" + word.getSpelling() + "] stored in cache.");
    }

    public void invalidateBySpelling(final Word word) {
        if (word == null) {
            return;
        }

        final String key = getKeyForSpelling(word.getSpelling());
        wordsBySpelling.remove(key);
        log.debug("@WC006 Word [" + word.getSpelling() + "] cleared from cache.");
    }

    public Set<String> getWordsForSearchString(final String searchString) {
        if (searchString == null) {
            return null;
        }

        final String key = getKeyForSearchString(searchString);
        Set<String> results = searchResults.get(key);
        
        if (results != null) {
            log.debug("@WC008 Search result found and returning from cache.");
            return results;
        }

        log.debug("@WC009 Search result not found on cache for spelling: '" + searchString + "'");
        return null;
    }

    public void cacheWordsForSearchString(final String searchString, final Set<String> spellings) {
        if (searchString == null || spellings == null) {
            return;
        }

        final String key = getKeyForSearchString(searchString);
        searchResults.put(key, spellings);
        log.info("@WC011 Storing search results on cache. Count: " + spellings.size() + ".");
    }

    private String getKeyForSpelling(String spelling) {
        return String.format("SP_%s", spelling);
    }

    private String getKeyForSearchString(String searchString) {
        return String.format("SS_%s", searchString);
    }

    public void flushCache() {
        wordsBySpelling.clear();
        searchResults.clear();
        log.info("@WC013 Cache flushed");
    }
}
