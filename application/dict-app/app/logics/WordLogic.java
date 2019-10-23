package logics;

import com.google.common.base.Preconditions;
import daos.UserRequestDao;
import daos.UserRequestDaoMongoImpl;
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
    private final UserRequestDao userRequestDao;

    private static final ShobdoLogger logger = new ShobdoLogger(WordLogic.class);

    public static WordLogic createMongoBackedWordLogic() {
        return new WordLogic(new WordDaoMongoImpl(), WordCache.getCache(), new UserRequestDaoMongoImpl());
    }

    public WordLogic(final WordDao wordDao,
                     final WordCache wordCache,
                     final UserRequestDao userRequestDao) {
        this.wordDao = wordDao;
        this.wordCache = wordCache;
        this.userRequestDao = userRequestDao;
    }

    private String generateWordId() {
        return String.format("%s-%s", Constants.WORD_ID_PREFIX, UUID.randomUUID());
    }

    public String generateMeaningId() {
        return String.format("%s-%s", Constants.MEANING_ID_PREFIX, UUID.randomUUID());
    }

    private String generateUserRequestId() {
        return String.format("%s-%s", Constants.REQUEST_ID_PREFIX, UUID.randomUUID());
    }

    /* Create */
    private void validateCreateWordObject(final Word word) {
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

        word.setId(generateWordId());
        wordDao.create(word);
        wordCache.cacheWord(word);
        return word;
    }

    //The following creates a word update request and returns the id
    public String createUserRequestForWordCreation(final JsonNode wordJNode) {

        final Word createWord = (Word) JsonUtil.jNodeToObject(wordJNode, Word.class);
        validateCreateWordObject(createWord);

        final UserRequest createRequest = UserRequest.builder()
            .id(generateUserRequestId())
            .targetInfo(new HashMap<>())
            .targetType(TargetType.WORD)
            .operation(RequestOperation.CREATE)
            .requestBody(JsonUtil.objectToJNode(createWord))
            .build();

        return userRequestDao.create(createRequest).getId();
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
    private void validateUpdateWordObject(final Word updateWord) {
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

    /* package private */ Word updateWord(final Word word) {
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

    //The following creates a word update request and returns the id
    private String createUserRequestForWordUpdate(final String wordId, final JsonNode wordJsonNode) {

        final Word updateWord = (Word) JsonUtil.jNodeToObject(wordJsonNode, Word.class);
        updateWord.setId(wordId);
        validateUpdateWordObject(updateWord);

        final Map<TargetType, String> targetInfo = new HashMap<>();
        targetInfo.put(TargetType.WORD, updateWord.getId());

        final UserRequest updateRequest = UserRequest.builder()
            .id(generateUserRequestId())
            .targetInfo(targetInfo)
            .targetType(TargetType.WORD)
            .operation(RequestOperation.UPDATE)
            .requestBody(JsonUtil.objectToJNode(updateWord))
            .build();

        return userRequestDao.create(updateRequest).getId();
    }

    /* Delete Word */
    public void deleteWord(final String wordId) {
        final Word word = getWordById(wordId);
        word.setStatus(EntityStatus.DELETED);
        wordDao.update(word);
        wordCache.invalidateWord(word);
    }

    private String createUserRequestForWordDeletion(final String wordId) {

        final Map<TargetType, String> targetInfo = new HashMap<>();
        targetInfo.put(TargetType.WORD, wordId);

        final UserRequest deleteRequest = UserRequest.builder()
            .id(generateUserRequestId())
            .targetInfo(targetInfo)
            .targetType(TargetType.WORD)
            .operation(RequestOperation.DELETE)
            .build();

        return userRequestDao.create(deleteRequest).getId();
    }

    private UserRequest getRequest(@NotNull final String requestId) {
        if (requestId == null || requestId.trim().length() == 0) {
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY + requestId);
        }
        return userRequestDao.get(requestId);
    }

    //todo make transactional
    //approveUserRequest applies the requested changes to a word
    public boolean approveUserRequest(final String requestId) {

        final UserRequest request  = getRequest(requestId);
        final String wordId;
        switch (request.getTargetType()) {
            case WORD:
                switch (request.getOperation()) {
                    case CREATE:
                        createWord(request.getRequestBody());
                        break;
                    case UPDATE:
                        wordId = Preconditions.checkNotNull(request.getTargetInfo().get(TargetType.WORD));
                        updateWord(wordId, request.getRequestBody());
                        break;
                    case DELETE:
                        wordId = Preconditions.checkNotNull(request.getTargetInfo().get(TargetType.WORD));
                        deleteWord(wordId);
                        break;
                }
                break;
            case MEANING:
                final String meaningId;
                switch (request.getOperation()) {
                    case CREATE:
                        wordId = Preconditions.checkNotNull(request.getTargetInfo().get(TargetType.WORD));
                        createMeaning(wordId, request.getRequestBody());
                        break;
                    case UPDATE:
                        wordId = Preconditions.checkNotNull(request.getTargetInfo().get(TargetType.WORD));
                        meaningId = Preconditions.checkNotNull(request.getTargetInfo().get(TargetType.MEANING));
                        updateMeaning(wordId, meaningId, request.getRequestBody());
                        break;
                    case DELETE:
                        wordId = Preconditions.checkNotNull(request.getTargetInfo().get(TargetType.WORD));
                        meaningId = Preconditions.checkNotNull(request.getTargetInfo().get(TargetType.MEANING));
                        deleteMeaning(wordId, meaningId);
                        break;
                }
                break;
        }
        saveRequestAsMerged("deleterId", request);
        return true;
    }

    private void saveRequestAsMerged(final String approverId, final UserRequest request) {
        request.setStatus(EntityStatus.DELETED);
        request.setDeleterId(approverId);
        final String deletionDateString = (new DateTime(DateTimeZone.UTC)).toString();
        request.setDeletedDate(deletionDateString);
        userRequestDao.update(request);
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
    private void validateCreateMeaningObject(final String wordId, final Meaning meaning) {
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

    //The following creates a word update request and returns the id
    public String createUserRequestForMeaningCreation(final String wordId, final JsonNode meaningJNode) {

        final Meaning meaning = (Meaning) JsonUtil.jNodeToObject(meaningJNode, Meaning.class);

        validateCreateMeaningObject(wordId, meaning);

        final Map<TargetType, String> targetInfo = new HashMap<>();
        targetInfo.put(TargetType.WORD, wordId);

        final UserRequest createRequest = UserRequest.builder()
            .id(generateUserRequestId())
            .targetInfo(targetInfo)
            .targetType(TargetType.MEANING)
            .operation(RequestOperation.CREATE)
            .requestBody(JsonUtil.objectToJNode(meaning))
            .build();

        return userRequestDao.create(createRequest).getId();
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
    private void validateUpdateMeaningObject(final String wordId, final Meaning meaning) {
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

    //The following creates a word update request and returns the id
    private String createUserRequestForMeaningUpdate(final String wordId, final JsonNode meaningJNode) {

        final Meaning updateMeaning = (Meaning) JsonUtil.jNodeToObject(meaningJNode, Meaning.class);
        validateUpdateMeaningObject(wordId, updateMeaning);

        final Map<TargetType, String> targetInfo = new HashMap<>();
        targetInfo.put(TargetType.WORD, wordId);
        targetInfo.put(TargetType.MEANING, updateMeaning.getId());

        final UserRequest updateRequest = UserRequest.builder()
            .id(generateUserRequestId())
            .targetInfo(targetInfo)
            .targetType(TargetType.MEANING)
            .operation(RequestOperation.UPDATE)
            .requestBody(JsonUtil.objectToJNode(updateMeaning))
            .build();

        return userRequestDao.create(updateRequest).getId();
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

    //The following creates a word update request and returns the id
    private String createUserRequestForMeaningUpdate(final String wordId, final String meangingId) {

        final Map<TargetType, String> targetInfo = new HashMap<>();
        targetInfo.put(TargetType.WORD, wordId);
        targetInfo.put(TargetType.MEANING, meangingId);

        final UserRequest updateRequest = UserRequest.builder()
            .id(generateUserRequestId())
            .targetInfo(targetInfo)
            .targetType(TargetType.MEANING)
            .operation(RequestOperation.DELETE)
            .build();

        return userRequestDao.create(updateRequest).getId();
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
