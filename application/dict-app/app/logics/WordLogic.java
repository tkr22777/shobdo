package logics;

import com.google.common.base.Preconditions;
import exceptions.EntityDoesNotExist;
import caches.WordCache;
import com.fasterxml.jackson.databind.JsonNode;
import daos.WordDaoMongoImpl;
import daos.WordDao;
import objects.*;
import utilities.*;

import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * Created by Tahsin Kabir on 8/14/16.
 */
public class WordLogic {

    private final WordDao wordDao;
    private final WordCache wordCache;

    private static final ShobdoLogger logger = new ShobdoLogger(WordLogic.class);

    public static WordLogic createMongoBackedWordLogic() {
        return new WordLogic(new WordDaoMongoImpl(), WordCache.getCache());
    }

    public WordLogic(final WordDao wordDao,
                     final WordCache wordCache) {
        this.wordDao = wordDao;
        this.wordCache = wordCache;
    }


    public String generateWordIdCheckDB() {
        int numberOfTries = 10;
        for (int i = 0; i < numberOfTries; i++) {
            String wordId = DictUtil.generateWordId();
            if (wordDao.getById(wordId) == null) {
                return wordId;
            }
        }
        throw new RuntimeException(String.format("Exhausted %s tries creating wordId", numberOfTries));
    }

    private String generateMeaningId() {
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
        word.setId(generateWordIdCheckDB());
        wordDao.create(word);
        wordCache.cacheWord(word);
        return word;
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

        wordCache.cacheWord(wordFromDB);
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
     */
    public Word updateWord(final Word word) {
        validateUpdateWordObject(word);
        final Word currentWord = getWordById(word.getId());

        //Only allow spelling, synonyms and antonyms to be updated
        final Word updatedWord = Word.builder()
            .id(currentWord.getId())
            .meanings(currentWord.getMeanings())
            .spelling(word.getSpelling())
            .synonyms(word.getSynonyms())
            .antonyms(word.getAntonyms())
            .build();

        wordDao.update(updatedWord); //update the entry in DB
        wordCache.cacheWord(updatedWord);
        return updatedWord;
    }

    /* Delete Word */
    public void deleteWord(final String wordId) {
        final Word word = getWordById(wordId);
        word.setStatus(EntityStatus.DELETED);
        wordDao.update(word);
        wordCache.invalidateWord(word);
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

        validateCreateMeaningObject(wordId, meaning);

        meaning.setId(generateMeaningId());

        final Word currentWord = getWordById(wordId);
        currentWord.addMeaningToWord(meaning);

        wordDao.update(currentWord);
        return meaning;
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
        validateUpdateMeaningObject(wordId,  meaning);
        final Word currentWord = getWordById(wordId);
        currentWord.getMeanings().put(meaning.getId(), meaning);
        updateWordWithCache(currentWord);
        return meaning;
    }


    /* DELETE meaning */
    public void deleteMeaning(final String wordId, final String meaningId) {
        final Word word = getWordById(wordId);
        if (word.getMeanings() == null || word.getMeanings().size() == 0) {
            return;
        }

        if (word.getMeanings().get(meaningId) != null) {
            word.getMeanings().remove(meaningId);
            updateWordWithCache(word);
        }
    }

    private void updateWordWithCache(final Word updatedWord) {
        wordDao.update(updatedWord); //update the entry in DB
        wordCache.cacheWord(updatedWord);
    }

    /* LIST meaning todo implement using WORD's interfaces */
    public ArrayList<Meaning> listMeanings(String wordId) {
        return new ArrayList<>();
    }
}
