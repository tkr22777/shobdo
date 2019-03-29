package logics;

import exceptions.EntityDoesNotExist;
import caches.WordCache;
import com.fasterxml.jackson.databind.JsonNode;
import daos.WordDaoMongoImpl;
import daos.WordDao;
import objects.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import utilities.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;

/**
 * Created by Tahsin Kabir on 8/14/16.
 */
public class WordLogic {

    private final WordDao wordDao;
    private final WordCache wordCache;

    private static final LogPrint logger = new LogPrint(WordLogic.class);

    public static WordLogic createMongoBackedWordLogic() {
        return new WordLogic(new WordDaoMongoImpl(), new WordCache());
    }

    public WordLogic(final WordDao wordDao, final WordCache wordCache) {
        this.wordDao = wordDao;
        this.wordCache = wordCache;
    }

    /* Create */
    public Word createWord(final JsonNode wordJsonNode) {
        final Word word = (Word) JsonUtil.jsonNodeToObject(wordJsonNode, Word.class);
        return createWord(word);
    }

    private String generateWordId() {
        return String.format("%s-%s", Constants.WORD_ID_PREFIX, UUID.randomUUID());
    }

    public Word createWord(final Word word) {

        if (word.getId() != null) {
            throw new IllegalArgumentException(Constants.Messages.UserProvidedIdForbidden(word.getId()));
        }

        if (word.getWordSpelling() == null || word.getWordSpelling().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.WORDSPELLING_NULLOREMPTY);
        }

        final Word existingWord = wordDao.getBySpelling(word.getWordSpelling());
        if (existingWord != null) {
            throw new IllegalArgumentException(Constants.Messages.WordSpellingExists(word.getWordSpelling()));
        }

        //word creation does not accept meanings
        if (word.getMeaningsMap() != null && word.getMeaningsMap().size() > 0) {
            throw new IllegalArgumentException(Constants.MEANING_PROVIDED);
        }

        word.setId(generateWordId());
        wordDao.create(word);
        wordCache.cacheWord(word);
        return word;
    }

    public void createWords(final Collection<Word> words) {
        words.forEach(this::createWord);
    }

    //Todo add toAPIJsonNode to the word object
    /* GET word by id */
    public Word getWordById(@NotNull final String wordId) {
        if (wordId == null || wordId.trim().length() == 0) {
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY + wordId);
        }
        final Word word = wordDao.getById(wordId);
        if (word == null) {
            throw new EntityDoesNotExist(Constants.ENTITY_NOT_FOUND + wordId);
        }
        return word;
    }

    /* GET word by (exact) spelling */
    public Word getWordBySpelling(final String spelling) {
        if (spelling == null || spelling.trim().length() == 0) {
            throw new IllegalArgumentException(Constants.WORDSPELLING_NULLOREMPTY);
        }

        final Word cachedWord = wordCache.getBySpelling(spelling);
        if (cachedWord != null) {
            return cachedWord;
        }

        final Word wordFromDB = wordDao.getBySpelling(spelling);
        if (wordFromDB == null) {
            throw new EntityDoesNotExist(Constants.ENTITY_NOT_FOUND + spelling);
        }
        if (wordFromDB != null) {
            wordCache.cacheWord(wordFromDB);
        }
        return wordFromDB;
    }

    /* Update */
    private void checkValidUpdateWordRequest(final Word updateWord) {
        if (updateWord.getId() == null || updateWord.getId().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);
        }

        if (updateWord.getWordSpelling() == null || updateWord.getWordSpelling().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.WORDSPELLING_NULLOREMPTY);
        }

        if (updateWord.getMeaningsMap() != null && updateWord.getMeaningsMap().size() > 0) {
            throw new IllegalArgumentException(Constants.MEANING_PROVIDED);
        }

        if (getWordById(updateWord.getId()) == null) {
            throw new IllegalArgumentException(Constants.ENTITY_NOT_FOUND + updateWord.getId());
        }
    }

    public Word updateWord(final String wordId, final JsonNode wordJsonNode) {
        final Word word = (Word) JsonUtil.jsonNodeToObject(wordJsonNode, Word.class);
        word.setId(wordId);
        return updateWord(word);
    }

    /* package private */ Word updateWord(final Word updateWordRequest) {
        checkValidUpdateWordRequest(updateWordRequest);
        final Word currentWord = getWordById(updateWordRequest.getId());

        //Only allow spelling, synonyms and antonyms to be updated
        final Word updatedWord = Word.builder()
            .id(currentWord.getId())
            .meaningsMap(currentWord.getMeaningsMap())
            .wordSpelling(updateWordRequest.getWordSpelling())
            .synonyms(updateWordRequest.getSynonyms())
            .antonyms(updateWordRequest.getAntonyms())
            .build();

        wordDao.update(updatedWord); //update the entry in DB
        wordCache.cacheWord(updatedWord);
        return updatedWord;
    }

    public Word updateWordVersioned(final String wordId, final JsonNode wordJsonNode) {
        final Word word = (Word) JsonUtil.jsonNodeToObject(wordJsonNode, Word.class);
        word.setId(wordId);
        return updateWordVersioned(word);
    }

    /* package private */ private Word updateWordVersioned(final Word updateWord) {
        try {
            final String requestId = createUpdateWordUserRequest(updateWord);
            return approveWordRequest(requestId);
        } catch (Exception ex) {
            if (ex instanceof  IllegalArgumentException) {
                throw ex;
            }
            throw new InternalError("Word update failed : " + ex.getStackTrace().toString());
        }
    }

    //The following creates a word update request and returns the id
    public String createUpdateWordUserRequest(final Word updateWord) {

        checkValidUpdateWordRequest(updateWord);

        final UserRequest updateRequest = UserRequest.builder()
            .id(generateNewWordUpdateReqId())
            .targetId(updateWord.getId())
            .operation(RequestOperation.UPDATE)
            .requestBody(JsonUtil.objectToJsonNode(updateWord))
            .build();

        return wordDao.createUserRequest(updateRequest).getId();
    }

    private UserRequest getRequest(@NotNull final String requestId) {
        if (requestId == null || requestId.trim().length() == 0) {
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY + requestId);
        }
        return wordDao.getUserRequest(requestId);
    }

    //todo make transactional
    //approveWordRequest applies the requested changes to a word
    public Word approveWordRequest(final String requestId) {

        final UserRequest storedRequest  = getRequest(requestId);
        switch (storedRequest.getOperation()) {
            case CREATE:
                return approveCreateWordRequest(storedRequest);
            case UPDATE:
                return approveUpdateWordRequest(storedRequest);
            case DELETE:
                return approveDeleteWordRequest(storedRequest);
            default:
                return null;
        }
    }

    private Word approveCreateWordRequest(final UserRequest request){
        //TODO implement
        return null;
    }

    private Word approveUpdateWordRequest(final UserRequest request){

        //Todo implement or rethink about backup
        //Word currentWordBackup = deepCopyWord(currentWord);
        //currentWordBackup = modifyToDeactivatedWordEntry(currentWordBackup);

        //Updated the word with the updates
        final Word updatedWord = updateWord(request.getId(), request.getRequestBody());
        saveRequestAsMerged(request); //Update the request as merged
        return updatedWord;
    }

    private void updateWordWithCache(final Word updatedWord) {
        wordDao.update(updatedWord); //update the entry in DB
        wordCache.cacheWord(updatedWord);
    }

    private Word modifyToDeactivatedWordEntry(final Word currentWordCopy) {

        //keeping the old word in the current words extra meta map with id
        currentWordCopy.setId(generateWordId());

        final String currentWordId = currentWordCopy.getId();
        currentWordCopy.setStatus(EntityStatus.DELETED);
        final String deletionDateString = (new DateTime(DateTimeZone.UTC)).toString();
        currentWordCopy.setDeletedDate(deletionDateString);

        return currentWordCopy;
    }

    private void saveRequestAsMerged(final UserRequest request) {

        request.setStatus(EntityStatus.DELETED);
        final String deletionDateString = (new DateTime(DateTimeZone.UTC)).toString();
        request.setDeletedDate(deletionDateString);
        wordDao.updateUserRequest(request);
    }

    private Word approveDeleteWordRequest(final UserRequest request){
        return null;
    }

    public static Word deepCopyWord(final Word word) {
        return (Word) JsonUtil.jsonNodeToObject(JsonUtil.objectToJsonNode(word), Word.class);
    }

    public static Meaning deepCopyMeaning(final Meaning meaning) {
        return (Meaning) JsonUtil.jsonNodeToObject(JsonUtil.objectToJsonNode(meaning), Meaning.class);
    }

    public static String generateNewWordUpdateReqId() {
        return Constants.REQUEST_ID_PREFIX + "-" + UUID.randomUUID();
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
    public Set<String> searchWords(final String searchSting) {
        return searchWords(searchSting, Constants.SEARCH_SPELLING_LIMIT);
    }

    public Set<String> searchWords(final String searchString, final int limit){
        if (searchString == null || searchString.trim().length() == 0) {
            return new HashSet<>();
        }

        Set<String> words = wordCache.getWordsForSearchString(searchString);
        if (words != null && words.size() > 0) {
            return words;
        }

        words = wordDao.searchSpellingsBySpelling(searchString, limit);
        if (words != null && words.size() > 0 ) {
            logger.info("@WL002 search result [size:" + words.size() + "] for spelling:\"" + searchString + "\" found in database and returning");
            wordCache.cacheWordsForSearchString(searchString, words);
            return words;
        }

        logger.info("@WL003 search result for spelling:\"" + searchString + "\" not found in database");
        return new HashSet<>();
    }

    public long totalWordCount(){
        return wordDao.totalCount();
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
    public Meaning createMeaning(final String wordId, final JsonNode meaningJsonNode) {
        final Meaning meaning = (Meaning) JsonUtil.jsonNodeToObject(meaningJsonNode, Meaning.class);
        return createMeaning(wordId, meaning);
    }

    public List<Meaning> createMeaningsBatch(final String wordId, final Collection<Meaning> meanings) {

        final ArrayList<Meaning> createdMeaning = new ArrayList<>();
        for (Meaning meaning: meanings) {
            createdMeaning.add(createMeaning(wordId,meaning) );
        }
        return createdMeaning;
    }

    public Meaning createMeaning(final String wordId, final Meaning meaning) {

        if (meaning.getId() != null) {
            throw new IllegalArgumentException(Constants.Messages.UserProvidedIdForbidden(meaning.getId()));
        }

        if (meaning.getMeaning() == null || meaning.getMeaning().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MEANING_NULLOREMPTY);
        }

        final Word currentWord = getWordById(wordId);

        if (currentWord == null) {
            throw new IllegalArgumentException(Constants.ENTITY_NOT_FOUND + wordId);
        }

        final String meaningId = generateNewMeaningId();
        meaning.setId(meaningId);
        currentWord.addMeaningToWord(meaning);

        wordDao.update(currentWord);
        return meaning;
    }

    public static String generateNewMeaningId() {
        return Constants.MEANING_ID_PREFIX + UUID.randomUUID();
    }

    /* GET meaning */
    public Meaning getMeaning(final String wordId, final String meaningId) {
        final Word word = getWordById(wordId);
        final Meaning meaning = word.getMeaningsMap() == null ? null : word.getMeaningsMap().get(meaningId);
        if (meaning == null) {
            throw new EntityDoesNotExist(Constants.ENTITY_NOT_FOUND + meaningId);
        }
        return meaning;
    }

    /* UPDATE meaning todo implement using WORD's interfaces */
    public Meaning updateMeaning(final String wordId, final String meaningId, final JsonNode meaningJsonNode) {
        final Meaning meaning = (Meaning) JsonUtil.jsonNodeToObject(meaningJsonNode, Meaning.class);
        return updateMeaning(wordId, meaningId, meaning);
    }

    public Meaning updateMeaning(final String wordId, final String meaningId, final Meaning meaning) {

        if (meaningId == null || !meaningId.equalsIgnoreCase(meaning.getId())) {
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);
        }

        if (meaning.getMeaning() == null || meaning.getMeaning().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MEANING_NULLOREMPTY);
        }

        final Word currentWord = getWordById(wordId);
        final Meaning currentMeaning = currentWord.getMeaningsMap().get(meaningId);

        if (currentMeaning == null) {
            throw new EntityDoesNotExist(Constants.ENTITY_NOT_FOUND + meaning.getId());
        }

        currentWord.getMeaningsMap().put(meaning.getId(), meaning);
        updateWordWithCache(currentWord);
        return meaning;
    }

    /* DELETE meaning */
    public void deleteMeaning(final String wordId, final String meaningId) {
        final Word word = getWordById(wordId);
        if (word.getMeaningsMap() == null || word.getMeaningsMap().size() == 0) {
            return;
        }

        if (word.getMeaningsMap().get(meaningId) != null) {
            word.getMeaningsMap().remove(meaningId);
            updateWordWithCache(word);
        }
    }

    /* LIST meaning todo implement using WORD's interfaces */
    public ArrayList<Meaning> listMeanings(String wordId) {
        return new ArrayList<>();
    }
}
