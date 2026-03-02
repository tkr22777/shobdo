package word;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Striped;
import common.objects.EntityStatus;
import exceptions.EntityDoesNotExist;
import com.fasterxml.jackson.databind.JsonNode;
import utilities.*;
import word.caches.WordCache;
import word.objects.Meaning;
import word.objects.Word;
import word.stores.WordStore;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

public class WordLogic {

    private final WordStore wordStore;
    private final WordCache wordCache;

    private static final ShobdoLogger logger = new ShobdoLogger(WordLogic.class);

    public WordLogic(final WordStore wordStore,
                     final WordCache wordCache) {
        this.wordStore = wordStore;
        this.wordCache = wordCache;
    }

    private String generateWordId() {
        return String.format("%s-%s", Constants.PREFIX_WORD_ID, UUID.randomUUID());
    }

    private String generateMeaningId() {
        /* TODO validate unique meaningId generation by having a reverse lookup */
        return String.format("%s-%s", Constants.PREFIX_MEANING_ID, UUID.randomUUID());
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

        Preconditions.checkNotNull(word.getSpelling(), Constants.MESSAGES_SPELLING_NULLOREMPTY);
        if (word.getSpelling().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MESSAGES_SPELLING_NULLOREMPTY);
        }

        final Word existingWord = wordStore.getBySpelling(word.getSpelling());
        if (existingWord != null) {
            throw new IllegalArgumentException(Constants.Messages.SpellingExists(word.getSpelling()));
        }

        //word creation does not accept meanings
        if (word.getMeanings() != null && word.getMeanings().size() > 0) {
            throw new IllegalArgumentException(Constants.MESSAGES_MEANING_PROVIDED);
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
            String wordId = generateWordId();
            synchronized (wordId.intern()) {
                if (wordStore.getById(wordId) == null) {
                    word.setId(wordId);
                    wordStore.create(word);
                    wordCache.cacheBySpelling(word);
                    return word;
                }
            }
        }
        throw new IllegalStateException(String.format("Failed to generate unique word ID after %s attempts", numberOfTries));
    }

    /* GET the word of the day — deterministic for all users on the same UTC date */
    public Word getWordOfDay() {
        final long total = wordStore.count();
        if (total == 0) {
            throw new EntityDoesNotExist("No words available in the dictionary");
        }
        final long daysSinceEpoch = java.time.LocalDate.now(java.time.ZoneOffset.UTC).toEpochDay();
        final int index = (int) (daysSinceEpoch % total);
        final Word word = wordStore.getWordAtIndex(index);
        if (word == null) {
            throw new EntityDoesNotExist("Word of the day not found at index " + index);
        }
        return word;
    }

    /* GET a random word */
    public Word getRandomWord() {
        final Word word = wordStore.getRandomWord();
        if (word == null) {
            throw new EntityDoesNotExist("No words available in the dictionary");
        }
        return word;
    }

    /* GET word by id */
    public Word getWordById(@NotNull final String wordId) {
        if (wordId == null || wordId.trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MESSAGES_ID_NULLOREMPTY + wordId);
        }
        final Word word = wordStore.getById(wordId);
        if (word == null) {
            throw new EntityDoesNotExist(Constants.Messages.EntityNotFound(wordId));
        }
        return word;
    }

    /* GET word by (exact) spelling */
    public Word getWordBySpelling(@NotNull final String spelling) {
        if (spelling == null || spelling.trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MESSAGES_SPELLING_NULLOREMPTY);
        }

        final Word cachedWord = wordCache.getBySpelling(spelling);
        if (cachedWord != null) {
            return cachedWord;
        }

        final Word wordFromDB = wordStore.getBySpelling(spelling);
        if (wordFromDB == null) {
            throw new EntityDoesNotExist(Constants.Messages.EntityNotFound(spelling));
        }

        wordCache.cacheBySpelling(wordFromDB);
        return wordFromDB;
    }

    /* Update */
    public void validateUpdateWordObject(final Word updateWord) {
        if (updateWord.getId() == null || updateWord.getId().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MESSAGES_ID_NULLOREMPTY);
        }

        if (updateWord.getSpelling() == null || updateWord.getSpelling().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MESSAGES_SPELLING_NULLOREMPTY);
        }

        if (updateWord.getMeanings() != null && updateWord.getMeanings().size() > 0) {
            throw new IllegalArgumentException(Constants.MESSAGES_MEANING_PROVIDED);
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
        final Word currentWord = getWordById(word.getId());
        final Word updatedWord = Word.builder()
            .id(currentWord.getId())
            .meanings(currentWord.getMeanings())
            .spelling(word.getSpelling())
            .build();
        wordStore.update(updatedWord); //update the entry in DB
        wordCache.cacheBySpelling(updatedWord);
        return updatedWord;
    }

    /* Delete Word */
    public void deleteWord(final String wordId) {
        final Word word = getWordById(wordId);
        word.setStatus(EntityStatus.DELETED);
        wordStore.update(word);
        wordCache.invalidateBySpelling(word);
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
     Embeddings of characters in a word?
     */
    public List<Word> searchWords(final String searchString) {

        if (searchString == null || searchString.trim().length() == 0) {
            return new ArrayList<>();
        }

        List<Word> words = wordCache.getWordsForSearchString(searchString);
        if (words != null && words.size() > 0) {
            return words;
        }

        words = wordStore.searchWords(searchString, Constants.SEARCH_SPELLING_LIMIT);
        if (words != null && words.size() > 0) {
            logger.info("@WL002 search result [size:" + words.size() + "] for spelling:\"" + searchString + "\" found in database and returning");
            wordCache.cacheWordsForSearchString(searchString, words);
            return words;
        }

        logger.info("@WL003 search result for spelling:\"" + searchString + "\" not found in database");
        return new ArrayList<>();
    }

    public long totalWordCount(){
        return wordStore.count();
    }

    public void deleteAllWords(){
        wordStore.deleteAll();
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

        if (meaning.getText() == null || meaning.getText().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MESSAGES_MEANING_NULLOREMPTY);
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
        currentWord.putMeaning(meaning);
        wordStore.update(currentWord);
        wordCache.cacheBySpelling(currentWord);
        return meaning;
    }

    /* GET meaning */
    public Meaning getMeaning(final String wordId, final String meaningId) {
        final Meaning meaning = getWordById(wordId).getMeaning(meaningId);
        if (meaning == null) {
            throw new EntityDoesNotExist(Constants.Messages.EntityNotFound(meaningId));
        }
        return meaning;
    }

    /* UPDATE meaning todo implement using WORD's interfaces */
    public void validateUpdateMeaningObject(final String wordId, final Meaning meaning) {
        if (meaning.getId() == null) {
            throw new IllegalArgumentException(Constants.MESSAGES_ID_NULLOREMPTY);
        }

        if (meaning.getText() == null || meaning.getText().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MESSAGES_MEANING_NULLOREMPTY);
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
        validateUpdateMeaningObject(wordId, meaning);
        final Word word = getWordById(wordId);
        word.putMeaning(meaning);
        wordStore.update(word); //update the entry in DB
        wordCache.cacheBySpelling(word);
        return meaning;
    }

    /* DELETE meaning */
    public void deleteMeaning(final String wordId, final String meaningId) {
        final Word word = getWordById(wordId);
        if (word.getMeanings().size() == 0) {
            return;
        }

        if (word.getMeanings().get(meaningId) != null) {
            word.getMeanings().remove(meaningId);
            wordStore.update(word); //update the entry in DB
            wordCache.cacheBySpelling(word);
        }
    }

    public List<Meaning> listMeanings(String wordId) {
        return getWordById(wordId).getMeanings().entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
    }

    public String addAntonym(final String wordId, final String meaningId, final String antonym) {
        final Word word = getWordById(wordId);
        final Meaning meaning = word.getMeanings().get(meaningId);
        meaning.addAntonym(antonym);
        word.putMeaning(meaning);
        wordStore.update(word);
        wordCache.cacheBySpelling(word);
        return antonym;
    }

    public void removeAntonym(final String wordId, final String meaningId, final String antonym) {
        final Word word = getWordById(wordId);
        final Meaning meaning = word.getMeanings().get(meaningId);
        meaning.removeAntonym(antonym);
        word.putMeaning(meaning);
        wordStore.update(word);
        wordCache.cacheBySpelling(word);
    }

    public String addSynonym(final String wordId, final String meaningId, final String synonym) {
        final Word word = getWordById(wordId);
        final Meaning meaning = word.getMeanings().get(meaningId);
        meaning.addSynonym(synonym);
        word.putMeaning(meaning);
        wordStore.update(word);
        wordCache.cacheBySpelling(word);
        return synonym;
    }

    public void removeSynonym(final String wordId, final String meaningId, final String synonym) {
        final Word word = getWordById(wordId);
        final Meaning meaning = word.getMeanings().get(meaningId);
        meaning.removeSynonym(synonym);
        word.putMeaning(meaning);
        wordStore.update(word);
        wordCache.cacheBySpelling(word);
    }
}
