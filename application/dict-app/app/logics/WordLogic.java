package logics;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Striped;
import exceptions.EntityDoesNotExist;
import caches.WordCache;
import com.fasterxml.jackson.databind.JsonNode;
import daos.*;
import objects.*;
import utilities.*;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * Created by Tahsin Kabir on 8/14/16.
 */
public class WordLogic {

    private final WordDao wordDao;
    private final WordCache wordCache;
    /* TODO use distributed lock */
    private final Striped<Lock> locks;

    private static final ShobdoLogger logger = new ShobdoLogger(WordLogic.class);

    public static WordLogic createMongoBackedWordLogic() {
        return new WordLogic(new WordDaoMongoImpl(), WordCache.getCache());
    }

    public WordLogic(final WordDao wordDao,
                     final WordCache wordCache) {
        this.wordDao = wordDao;
        this.wordCache = wordCache;
        this.locks = Striped.lock(500);
    }

    private String generateMeaningId() {
        /* TODO validate unique meaningId generation by having a reverse lookup */
        return String.format("%s-%s", Constants.MEANING_ID_PREFIX, UUID.randomUUID());
    }

    /* Create */
    /* Validation criteria:
        1. No user provided Id
        2. Spelling cannot be null or empty
        3. Barred to create word with an existing spelling.
        4. Cannot create word with meaning, user must add meaning later
     */
    public void validateCreateWordObject(final Word word) {
        if (word.getId() != null) {
            throw new IllegalArgumentException(Constants.Messages.UserProvidedIdForbidden(word.getId()));
        }

        Preconditions.checkNotNull(word.getSpelling(), Constants.SPELLING_NULLOREMPTY);
        if (word.getSpelling().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.SPELLING_NULLOREMPTY);
        }

        final Word existingWord = wordDao.getBySpelling(word.getSpelling());
        if (existingWord != null) {
            throw new IllegalArgumentException(Constants.Messages.SpellingExists(word.getSpelling()));
        }

        //word creation does not accept meanings
        if (word.getMeanings() != null && word.getMeanings().size() > 0) {
            throw new IllegalArgumentException(Constants.MEANING_PROVIDED);
        }
    }

    public Word createWord(final JsonNode wordJsonNode) {
        final Word word = (Word) JsonUtil.jNodeToObject(wordJsonNode, Word.class);
        return createWord(word);
    }

    public Word createWord(final Word word) {
        validateCreateWordObject(word);
        int numberOfTries = 10;
        for (int i = 0; i < numberOfTries; i++) {
            String wordId = DictUtil.generateWordId();
            synchronized (wordId.intern()) {
                if (wordDao.getById(wordId) == null) {
                    word.setId(wordId);
                    wordDao.create(word);
                    wordCache.cacheBySpelling(word);
                    return word;
                }
            }
        }
        throw new RuntimeException(String.format("Exhausted %s tries creating word", numberOfTries));
    }

    /* GET word by id */
    public Word getWordById(@NotNull final String wordId) {
        if (wordId == null || wordId.trim().length() == 0) {
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY + wordId);
        }
        final Word word = wordDao.getById(wordId);
        if (word == null) {
            throw new EntityDoesNotExist(Constants.Messages.EntityNotFound(wordId));
        }
        return word;
    }

    /* GET word by (exact) spelling */
    public Word getWordBySpelling(@NotNull final String spelling) {
        if (spelling == null || spelling.trim().length() == 0) {
            throw new IllegalArgumentException(Constants.SPELLING_NULLOREMPTY);
        }

        final Word cachedWord = wordCache.getBySpelling(spelling);
        if (cachedWord != null) {
            return cachedWord;
        }

        final Word wordFromDB = wordDao.getBySpelling(spelling);
        if (wordFromDB == null) {
            throw new EntityDoesNotExist(Constants.Messages.EntityNotFound(spelling));
        }

        wordCache.cacheBySpelling(wordFromDB);
        return wordFromDB;
    }

    /* Update */
    public void validateUpdateWordObject(final Word updateWord) {
        if (updateWord.getId() == null || updateWord.getId().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);
        }

        if (updateWord.getSpelling() == null || updateWord.getSpelling().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.SPELLING_NULLOREMPTY);
        }

        if (updateWord.getMeanings() != null && updateWord.getMeanings().size() > 0) {
            throw new IllegalArgumentException(Constants.MEANING_PROVIDED);
        }

        if (getWordById(updateWord.getId()) == null) {
            throw new IllegalArgumentException(Constants.Messages.EntityNotFound(updateWord.getId()));
        }
    }

    public Word updateWord(final String wordId, final JsonNode wordJsonNode) {
        final Word word = (Word) JsonUtil.jNodeToObject(wordJsonNode, Word.class);
        word.setId(wordId);
        return updateWord(word);
    }

    /*
      Spec: Word meanings cannot be updated using updateWord, stays as it is.
      Only allow spelling, synonyms and antonyms to be updated
     */
    public Word updateWord(final Word word) {
        validateUpdateWordObject(word);
        Lock wordLock = locks.get(word.getId());
        logger.info(wordLock.toString());
        try {
            wordLock.lock();
            logger.info(wordLock.toString());
            final Word currentWord = getWordById(word.getId());
            final Word updatedWord = Word.builder()
                .id(currentWord.getId())
                .meanings(currentWord.getMeanings())
                .spelling(word.getSpelling())
                .synonyms(word.getSynonyms())
                .antonyms(word.getAntonyms())
                .build();
            wordDao.update(updatedWord); //update the entry in DB
            wordCache.cacheBySpelling(updatedWord);
            return updatedWord;
        } finally {
            wordLock.unlock();
        }
    }

    /* Delete Word */
    public void deleteWord(final String wordId) {
        Lock wordLock = locks.get(wordId);
        try {
            wordLock.lock();
            final Word word = getWordById(wordId);
            word.setStatus(EntityStatus.DELETED);
            wordDao.update(word);
            wordCache.invalidateBySpelling(word);
        } finally {
            wordLock.unlock();
        }
    }

    /* LIST words todo */
    public ArrayList<Word> listWords(final String startWordId, final Integer limit) {
        return new ArrayList<>();
    }

    /* SEARCH words by a search string
     Cache all the spellings together for search!!
     Check if there is are ways to search by string on the indexed string, it should be very basic!
     ** You may return a smart object that specifies each close words and also suggestion if it didn't match
     How to find closest neighbour of a BanglaUtil word? you may be able to do that locally?
     */
    public Set<String> searchWords(final String searchString) {

        if (searchString == null || searchString.trim().length() == 0) {
            return new HashSet<>();
        }

        Set<String> words = wordCache.getWordsForSearchString(searchString);
        if (words != null && words.size() > 0) {
            return words;
        }

        words = wordDao.searchSpellingsBySpelling(searchString, Constants.SEARCH_SPELLING_LIMIT);
        if (words != null && words.size() > 0 ) {
            logger.info("@WL002 search result [size:" + words.size() + "] for spelling:\"" + searchString + "\" found in database and returning");
            wordCache.cacheWordsForSearchString(searchString, words);
            return words;
        }

        logger.info("@WL003 search result for spelling:\"" + searchString + "\" not found in database");
        return new HashSet<>();
    }

    public long totalWordCount(){
        return wordDao.count();
    }

    public void deleteAllWords(){
        wordDao.deleteAll();
    }

    public void flushCache(){
        wordCache.flushCache();
    }

    /** CRUDL for meaning of a word:
     * most of the operations will deal with getting the word
     * and modifying the word to incorporate the create, update
     * and delete changes of a meaning and updating the word back
     * to  DB. The update should take into account of caching, i.e.
     * invalidating and re-caching the changes.
     */

    /* CREATE meaning */
    public void validateCreateMeaningObject(final String wordId, final Meaning meaning) {
        if (meaning.getId() != null) {
            throw new IllegalArgumentException(Constants.Messages.UserProvidedIdForbidden(meaning.getId()));
        }

        if (meaning.getMeaning() == null || meaning.getMeaning().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MEANING_NULLOREMPTY);
        }

        final Word currentWord = getWordById(wordId);
        if (currentWord == null) {
            throw new IllegalArgumentException(Constants.Messages.EntityNotFound(wordId));
        }
    }

    public Meaning createMeaning(final String wordId, final JsonNode meaningJsonNode) {
        final Meaning meaning = (Meaning) JsonUtil.jNodeToObject(meaningJsonNode, Meaning.class);
        return createMeaning(wordId, meaning);
    }

    public Meaning createMeaning(final String wordId, final Meaning meaning) {

        Lock wordLock = locks.get(wordId);
        try {
            wordLock.lock();
            validateCreateMeaningObject(wordId, meaning);
            meaning.setId(generateMeaningId());
            final Word currentWord = getWordById(wordId);
            currentWord.addMeaningToWord(meaning);
            wordDao.update(currentWord);
            wordCache.cacheBySpelling(currentWord);
            return meaning;
        } finally {
            wordLock.unlock();
        }
    }

    /* GET meaning */
    public Meaning getMeaning(final String wordId, final String meaningId) {
        final Word word = getWordById(wordId);
        final Meaning meaning = word.getMeanings() == null ? null : word.getMeanings().get(meaningId);
        if (meaning == null) {
            throw new EntityDoesNotExist(Constants.Messages.EntityNotFound(meaningId));
        }
        return meaning;
    }

    /* UPDATE meaning todo implement using WORD's interfaces */
    public void validateUpdateMeaningObject(final String wordId, final Meaning meaning) {
        if (meaning.getId() == null) {
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);
        }

        if (meaning.getMeaning() == null || meaning.getMeaning().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MEANING_NULLOREMPTY);
        }

        final Word currentWord = getWordById(wordId);
        final Meaning currentMeaning = currentWord.getMeanings().get(meaning.getId());

        if (currentMeaning == null) {
            throw new EntityDoesNotExist(Constants.Messages.EntityNotFound(meaning.getId()));
        }
    }

    public Meaning updateMeaning(final String wordId, final String meaningId, final JsonNode meaningJsonNode) {
        final Meaning meaning = (Meaning) JsonUtil.jNodeToObject(meaningJsonNode, Meaning.class);
        meaning.setId(meaningId);
        return updateMeaning(wordId, meaning);
    }

    private Meaning updateMeaning(final String wordId, final Meaning meaning) {
        Lock wordLock = locks.get(wordId);
        try {
            wordLock.lock();
            validateUpdateMeaningObject(wordId, meaning);
            final Word word = getWordById(wordId);
            word.getMeanings().put(meaning.getId(), meaning);
            wordDao.update(word); //update the entry in DB
            wordCache.cacheBySpelling(word);
            return meaning;
        } finally {
          wordLock.unlock();
        }
    }


    /* DELETE meaning */
    public void deleteMeaning(final String wordId, final String meaningId) {
        Lock wordLock = locks.get(wordId);
        try {
            wordLock.lock();
            final Word word = getWordById(wordId);
            if (word.getMeanings() == null || word.getMeanings().size() == 0) {
                return;
            }

            if (word.getMeanings().get(meaningId) != null) {
                word.getMeanings().remove(meaningId);
                wordDao.update(word); //update the entry in DB
                wordCache.cacheBySpelling(word);
            }
        } finally {
            wordLock.unlock();
        }
    }

    /* LIST meaning TODO implement using WORD's interfaces */
    public ArrayList<Meaning> listMeanings(String wordId) {
        return new ArrayList<>();
    }

    /* ADD antonym */
    public Antonym addAntonym(final String wordId, final JsonNode antonymJson) {
        final Antonym antonym = (Antonym) JsonUtil.jNodeToObject(antonymJson, Antonym.class);
        return addAntonym(wordId, antonym.getSpelling(), antonym.getStrength());
    }

    public Antonym addAntonym(final String wordId,
                              final String antonymSpelling,
                              final int strength) {
        Lock wordLock = locks.get(wordId);
        logger.info(wordLock.toString());
        try {
            wordLock.lock();
            logger.info(wordLock.toString());
            final Word word = getWordById(wordId);
            Word antonymWord = null;
            try {
                antonymWord = getWordBySpelling(antonymSpelling);
            } catch (Exception ex) {
                /* It's okay, there is no corresponding word for the antonym */
            }

            final Antonym antonym = Antonym.builder()
                .spelling(antonymSpelling)
                .targetWordId(antonymWord == null ? null : antonymWord.getId())
                .strength(strength)
                .build();

            word.addAntonym(antonym);
            wordDao.update(word);
            wordCache.cacheBySpelling(word);
            return antonym;
        } finally {
            wordLock.unlock();
        }
    }

    /* REMOVE antonym */
    public void removeAntonym(final String wordId, final JsonNode antonymJson) {
        final Antonym antonym = (Antonym) JsonUtil.jNodeToObject(antonymJson, Antonym.class);
        removeAntonym(wordId, antonym.getSpelling());
    }

    public void removeAntonym(final String wordId, final String antonymSpelling) {

        Lock wordLock = locks.get(wordId);
        try {
            wordLock.lock();
            final Word word = getWordById(wordId);
            word.removeAntonym(Antonym.builder().spelling(antonymSpelling).build());
            wordDao.update(word);
            wordCache.cacheBySpelling(word);
        } finally {
            wordLock.unlock();
        }
    }

    /* ADD synonym */
    public Synonym addSynonym(final String wordId, final JsonNode synonymJson) {
        final Synonym synonym = (Synonym) JsonUtil.jNodeToObject(synonymJson, Synonym.class);
        return addSynonym(wordId, synonym.getSpelling(), synonym.getStrength());
    }

    public Synonym addSynonym(final String wordId,
                              final String synonymSpelling,
                              final int strength) {

        Lock wordLock = locks.get(wordId);
        try {
            wordLock.lock();
            final Word word = getWordById(wordId);
            Word synonymWord = null;
            try {
                synonymWord = getWordBySpelling(synonymSpelling);
            } catch (EntityDoesNotExist entityDoesNotExistEx) {
                //It is okay, could not find a corresponding word for the given spelling
            }

            final Synonym synonym = Synonym.builder()
                .spelling(synonymSpelling)
                .targetWordId(synonymWord == null ? null : synonymWord.getId())
                .strength(strength)
                .build();

            word.addSynonym(synonym);
            wordDao.update(word);
            wordCache.cacheBySpelling(word);
            return synonym;
        } finally {
            wordLock.unlock();
        }
    }

    /* REMOVE synonym */
    public void removeSynonym(final String wordId, final JsonNode synonymJson) {
        final Synonym synonym = (Synonym) JsonUtil.jNodeToObject(synonymJson, Synonym.class);
        removeSynonym(wordId, synonym.getSpelling());
    }

    private void removeSynonym(final String wordId, final String synonymSpelling) {

        Lock wordLock = locks.get(wordId);
        try {
            wordLock.lock();
            final Word word = getWordById(wordId);
            word.removeSynonym(Synonym.builder().spelling(synonymSpelling).build());
            wordDao.update(word);
            wordCache.cacheBySpelling(word);
        } finally {
            wordLock.unlock();
        }
    }
}
