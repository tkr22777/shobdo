package logics;

import com.google.common.base.Preconditions;
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

    private String generateWordId() {
        return String.format("%s-%s", Constants.WORD_ID_PREFIX, UUID.randomUUID());
    }

    /* Create */
    private void validateCreateWordObject(final Word word) {
        if (word.getId() != null) {
            throw new IllegalArgumentException(Constants.Messages.UserProvidedIdForbidden(word.getId()));
        }

        Preconditions.checkNotNull(word.getWordSpelling(), Constants.WORDSPELLING_NULLOREMPTY);
        if (word.getWordSpelling().trim().length() == 0) {
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
    }

    public Word createWord(final JsonNode wordJsonNode) {
        final Word word = (Word) JsonUtil.jNodeToObject(wordJsonNode, Word.class);
        return createWord(word);
    }

    public Word createWord(final Word word) {
        validateCreateWordObject(word);

        word.setId(generateWordId());
        wordDao.create(word);
        wordCache.cacheWord(word);
        return word;
    }

    public String createWordWithUserRequest(final JsonNode wordJNode) {
        final Word word = (Word) JsonUtil.jNodeToObject(wordJNode, Word.class);
        return createWordWithUserRequest(word);
    }

    private String createWordWithUserRequest(final Word word) {
        try {
            final String requestId = createUserRequestForWordCreation(word);
            approveWordRequest(requestId);
            return requestId;
        } catch (Exception ex) {
            if (ex instanceof  IllegalArgumentException) {
                throw ex;
            }
            throw new InternalError("Word update failed : " + ex.getStackTrace().toString());
        }
    }

    //The following creates a word update request and returns the id
    public String createUserRequestForWordCreation(final Word createWord) {

        validateCreateWordObject(createWord);

        final UserRequest createRequest = UserRequest.builder()
            .id(generateUserRequestId())
            .targetId(null)
            .targetType(TargetType.WORD)
            .operation(RequestOperation.CREATE)
            .requestBody(JsonUtil.objectToJNode(createWord))
            .build();

        return wordDao.createUserRequest(createRequest).getId();
    }

    //Todo add toAPIJsonNode to the word object
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
            throw new IllegalArgumentException(Constants.WORDSPELLING_NULLOREMPTY);
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
    private void validateUpdateWordObject(final Word updateWord) {
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
            throw new IllegalArgumentException(Constants.Messages.EntityNotFound(updateWord.getId()));
        }
    }

    public Word updateWord(final String wordId, final JsonNode wordJsonNode) {
        final Word word = (Word) JsonUtil.jNodeToObject(wordJsonNode, Word.class);
        word.setId(wordId);
        return updateWord(word);
    }

    /* package private */ Word updateWord(final Word word) {
        validateUpdateWordObject(word);
        final Word currentWord = getWordById(word.getId());

        //Only allow spelling, synonyms and antonyms to be updated
        final Word updatedWord = Word.builder()
            .id(currentWord.getId())
            .meaningsMap(currentWord.getMeaningsMap())
            .wordSpelling(word.getWordSpelling())
            .synonyms(word.getSynonyms())
            .antonyms(word.getAntonyms())
            .build();

        wordDao.update(updatedWord); //update the entry in DB
        wordCache.cacheWord(updatedWord);
        return updatedWord;
    }

    public String updateWordWithUserRequest(final String wordId, final JsonNode wordJsonNode) {
        final Word word = (Word) JsonUtil.jNodeToObject(wordJsonNode, Word.class);
        word.setId(wordId);
        return updateWordWithUserRequest(word);
    }

    private String updateWordWithUserRequest(final Word updateWord) {
        try {
            final String requestId = createUserRequestForWordUpdate(updateWord);
            approveWordRequest(requestId);
            return requestId;
        } catch (Exception ex) {
            if (ex instanceof  IllegalArgumentException) {
                throw ex;
            }
            throw new InternalError("Word update failed : " + ex.getStackTrace().toString());
        }
    }

    //The following creates a word update request and returns the id
    private String createUserRequestForWordUpdate(final Word updateWord) {

        validateUpdateWordObject(updateWord);

        final UserRequest updateRequest = UserRequest.builder()
            .id(generateUserRequestId())
            .targetId(updateWord.getId())
            .operation(RequestOperation.UPDATE)
            .requestBody(JsonUtil.objectToJNode(updateWord))
            .build();

        return wordDao.createUserRequest(updateRequest).getId();
    }

    private UserRequest getRequest(@NotNull final String requestId) {
        if (requestId == null || requestId.trim().length() == 0) {
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY + requestId);
        }
        return wordDao.getUserRequest(requestId);
    }

    private Word approveCreateWordRequest(final UserRequest request){
        final Word createdWord = createWord(request.getRequestBody());
        saveRequestAsMerged(request);
        return createdWord;
    }

    private Word approveUpdateWordRequest(final UserRequest request){

        //Todo implement or rethink about backup
        final Word updatedWord = updateWord(request.getId(), request.getRequestBody());
        saveRequestAsMerged(request); //Update the request as merged
        return updatedWord;
    }

    private void approveDeleteWordRequest(final UserRequest request){
        deleteWord(request.getTargetId());
    }

    public static String generateUserRequestId() {
        return Constants.REQUEST_ID_PREFIX + "-" + UUID.randomUUID();
    }

    /* Delete Word */
    public void deleteWord(final String wordId) {
        final Word word = getWordById(wordId);
        word.setStatus(EntityStatus.DELETED);
        wordDao.update(word);
        wordCache.invalidateWord(word);
    }

    public String deleteWordWithUserRequest(final String wordId) {
        try {
            final String requestId = createUserRequestForWordDeletion(wordId);
            approveWordRequest(requestId);
            return requestId;
        } catch (Exception ex) {
            if (ex instanceof  IllegalArgumentException) {
                throw ex;
            }
            throw new InternalError("Word deletion failed : " + ex.getStackTrace().toString());
        }
    }

    private String createUserRequestForWordDeletion(final String wordId) {

        final UserRequest createRequest = UserRequest.builder()
            .id(generateUserRequestId())
            .targetId(wordId)
            .targetType(TargetType.WORD)
            .operation(RequestOperation.DELETE)
            .build();

        return wordDao.createUserRequest(createRequest).getId();
    }

    //todo make transactional
    //approveWordRequest applies the requested changes to a word
    public void approveWordRequest(final String requestId) {

        final UserRequest storedRequest  = getRequest(requestId);
        switch (storedRequest.getOperation()) {
            case CREATE:
                approveCreateWordRequest(storedRequest);
            case UPDATE:
                approveUpdateWordRequest(storedRequest);
            case DELETE:
                approveDeleteWordRequest(storedRequest);
        }
    }

    private void saveRequestAsMerged(final UserRequest request) {
        request.setStatus(EntityStatus.DELETED);
        request.setDeleterId("deleterId");
        final String deletionDateString = (new DateTime(DateTimeZone.UTC)).toString();
        request.setDeletedDate(deletionDateString);
        wordDao.updateUserRequest(request);
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

    public static String generateMeaningId() {
        return Constants.MEANING_ID_PREFIX + UUID.randomUUID();
    }

    /* CREATE meaning */
    public Meaning createMeaning(final String wordId, final JsonNode meaningJsonNode) {
        final Meaning meaning = (Meaning) JsonUtil.jNodeToObject(meaningJsonNode, Meaning.class);
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
            throw new IllegalArgumentException(Constants.Messages.EntityNotFound(wordId));
        }

        final String meaningId = generateMeaningId();
        meaning.setId(meaningId);
        currentWord.addMeaningToWord(meaning);

        wordDao.update(currentWord);
        return meaning;
    }


    /* GET meaning */
    public Meaning getMeaning(final String wordId, final String meaningId) {
        final Word word = getWordById(wordId);
        final Meaning meaning = word.getMeaningsMap() == null ? null : word.getMeaningsMap().get(meaningId);
        if (meaning == null) {
            throw new EntityDoesNotExist(Constants.Messages.EntityNotFound(meaningId));
        }
        return meaning;
    }

    /* UPDATE meaning todo implement using WORD's interfaces */
    public Meaning updateMeaning(final String wordId, final String meaningId, final JsonNode meaningJsonNode) {
        final Meaning meaning = (Meaning) JsonUtil.jNodeToObject(meaningJsonNode, Meaning.class);
        return updateMeaning(wordId, meaningId, meaning);
    }

    private Meaning updateMeaning(final String wordId, final String meaningId, final Meaning meaning) {

        if (meaningId == null || !meaningId.equalsIgnoreCase(meaning.getId())) {
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);
        }

        if (meaning.getMeaning() == null || meaning.getMeaning().trim().length() == 0) {
            throw new IllegalArgumentException(Constants.MEANING_NULLOREMPTY);
        }

        final Word currentWord = getWordById(wordId);
        final Meaning currentMeaning = currentWord.getMeaningsMap().get(meaningId);

        if (currentMeaning == null) {
            throw new EntityDoesNotExist(Constants.Messages.EntityNotFound(meaning.getId()));
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

    private void updateWordWithCache(final Word updatedWord) {
        wordDao.update(updatedWord); //update the entry in DB
        wordCache.cacheWord(updatedWord);
    }

    /* LIST meaning todo implement using WORD's interfaces */
    public ArrayList<Meaning> listMeanings(String wordId) {
        return new ArrayList<>();
    }
}
