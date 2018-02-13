package logics;

import Exceptions.EntityDoesNotExist;
import cache.WordCache;
import com.fasterxml.jackson.databind.JsonNode;
import daoImplementation.WordDaoMongoImpl;
import daos.WordDao;
import helpers.WordHelper;
import objects.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import play.libs.Json;
import utilities.*;

import java.util.*;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordLogic {

    private WordDao wordDao;
    private WordCache wordCache;

    private BenchmarkLogger bmLog = new BenchmarkLogger(WordLogic.class);
    private LogPrint log = new LogPrint(WordLogic.class);

    private static final String REQUEST_MERGED = "Requests Merged";

    public static WordLogic factory() {

        WordDao wordDao = new WordDaoMongoImpl();
        return new WordLogic( wordDao, new WordCache() );
    }

    public WordLogic( WordDao wordDao, WordCache wordCache) {

        this.wordDao = wordDao;
        this.wordCache = wordCache;
    }

    /* Create */
    public JsonNode createWord(JsonNode wordJsonNode) {

        Word word = (Word) JsonUtil.jsonNodeToObject(wordJsonNode, Word.class);
        Word createdWord = createWord(word) ;
        return convertWordToJsonResponse(createdWord);
    }

    public static String generateNewWordId() {
        return Constants.WORD_ID_PREFIX + "-" + UUID.randomUUID();
    }

    private JsonNode convertWordToJsonResponse(Word word) {

        JsonNode jsonNode = Json.toJson(word);
        List attributesToRemove = Arrays.asList("extraMetaMap", "entityMeta");
        return JsonUtil.removeFieldsFromJsonNode(jsonNode, attributesToRemove);
    }

    public Word createWord(Word word) {

        if(word.getId() != null)
            throw new IllegalArgumentException(Constants.CREATE_ID_NOT_PERMITTED + word.getId());

        if(word.getWordSpelling() == null || word.getWordSpelling().trim().length() == 0)
            throw new IllegalArgumentException(Constants.WORDSPELLING_NULLOREMPTY);

        Word existingWord = wordDao.getWordBySpelling( word.getWordSpelling() );
        if( existingWord != null)
            throw new IllegalArgumentException(Constants.CREATE_SPELLING_EXISTS + word.getWordSpelling());

        //word creation does not accept meanings
        if(word.getMeaningsMap() != null && word.getMeaningsMap().size() > 0)
            throw new IllegalArgumentException(Constants.MEANING_PROVIDED);

        String wordId = generateNewWordId();
        word.setId(wordId);

        //todo get creatorId from context
        String creatorId =  "sin";
        String creationDateString = (new DateTime(DateTimeZone.UTC)).toString();
        EntityMeta entityMeta = new EntityMeta(EntityStatus.ACTIVE, EntityType.WORD, null, creatorId,
                creationDateString, null, null, 0 );
        word.setEntityMeta(entityMeta);

        wordDao.createWord(word);
        wordCache.cacheWord(word);

        return word;
    }

    public void createWords(Collection<Word> words) {

        for(Word word: words)
            createWord(word);
    }

    /* GET word by id */
    public JsonNode getWordJNodeByWordId(String wordId) {

        Word word = getWordByWordId(wordId);
        return convertWordToJsonResponse(word);
    }

    public Word getWordByWordId(String wordId) {

        if( wordId == null || wordId.trim().length() == 0 )
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY + wordId);

        Word word = wordDao.getWordByWordId(wordId);

        if( word == null)
            throw new EntityDoesNotExist( Constants.ENTITY_NOT_FOUND + wordId);

        return word;
    }

    /* GET word by (exact) spelling */
    public JsonNode getWordJNodeBySpelling(String spelling) {

        Word word = getWordBySpelling(spelling);
        return word == null? null: convertWordToJsonResponse(word);
    }

    public Word getWordBySpelling(String spelling) {

        if(spelling == null || spelling.trim().length() == 0)
            throw new IllegalArgumentException("WLEX: getWordBySpelling word spelling is null or empty");

        Word cachedWord = wordCache.getWordBySpelling(spelling);

        if(cachedWord != null)
            return cachedWord;

        Word wordFromDB = wordDao.getWordBySpelling(spelling);

        if( wordFromDB == null)
            throw new EntityDoesNotExist( Constants.ENTITY_NOT_FOUND + spelling);

        wordCache.cacheWord(wordFromDB);
        return wordFromDB;
    }

    /* Update */

    public JsonNode updateWordJNode(String wordId, JsonNode wordJsonNode) {

        Word word = (Word) JsonUtil.jsonNodeToObject(wordJsonNode, Word.class);
        word.setId(wordId);
        Word updatedWord = updateWordVersioned(word);
        return convertWordToJsonResponse(updatedWord);
    }

    public Word updateWordVersioned(Word updateWord) {

        try {

            String requestId = createUpdateWordRequest(updateWord);
            return approveWordRequest( requestId );

        } catch (Exception ex) {

            if(ex instanceof  IllegalArgumentException)
                throw ex;

            throw new InternalError("Word update failed : " + ex.getStackTrace().toString());
        }
    }

    //todo make transactional
    //The following creates a word update request and returns the requestId
    public String createUpdateWordRequest(Word updateWord) {

        if(updateWord.getId() == null || updateWord.getId().trim().length() == 0)
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);

        if(updateWord.getWordSpelling() == null || updateWord.getWordSpelling().trim().length() == 0)
            throw new IllegalArgumentException(Constants.WORDSPELLING_NULLOREMPTY);

        if(updateWord.getMeaningsMap() != null && updateWord.getMeaningsMap().size() > 0)
            throw new IllegalArgumentException(Constants.MEANING_PROVIDED);

        String currentWordId = updateWord.getId();
        getWordByWordId(currentWordId); //validating the update request for the word exists

        //Create a UserRequest object for the word
        String requestId = generateNewWordUpdateReqID();
        String creatorId =  "sin";
        String creationDateString = (new DateTime(DateTimeZone.UTC)).toString();
        EntityMeta requestMeta = new EntityMeta( EntityStatus.ACTIVE, EntityType.REQUEST, null, creatorId,
                creationDateString, null, null, 0 );
        JsonNode body = JsonUtil.objectToJsonNode(updateWord);
        UserRequest updateRequest = new UserRequest(requestId, currentWordId, EntityType.WORD, RequestOperation.UPDATE,
                body, requestMeta);
        wordDao.createRequest(updateRequest);

        return requestId;
    }

    private UserRequest getRequest(String requestId) {

        if(requestId == null || requestId.trim().length() == 0)
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);

        UserRequest storedRequest = wordDao.getRequestById(requestId);

        if(storedRequest == null)
            throw new IllegalArgumentException( Constants.ENTITY_NOT_FOUND + requestId );

        if(!storedRequest.getEntityMeta().getStatus().equals(EntityStatus.ACTIVE))
            throw new IllegalArgumentException( Constants.ENTITY_IS_DEACTIVE + requestId );

        return storedRequest;
    }

    //todo make transactional
    //approveWordRequest applies the requested changes to a word
    public Word approveWordRequest(String requestId) {

        UserRequest storedRequest  = getRequest(requestId);

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

    private Word approveCreateWordRequest(UserRequest request){

        return null;
    }

    private Word approveUpdateWordRequest(UserRequest request){

        String currentWordId = request.getTargetId();
        Word currentWord = getWordByWordId(currentWordId);

        //Keeping backup of current word
        Word currentWordBackup = deepCopyWord(currentWord);
        currentWordBackup = modifyToDeactivatedWordEntry(currentWordBackup);

        //Updated the word with the updates
        JsonNode updateWordBody = request.getBody();
        Word updateRequestWord = (Word) JsonUtil.jsonNodeToObject( updateWordBody, Word.class);
        currentWord.setWordSpelling( updateRequestWord.getWordSpelling() );
        currentWord.setSynonyms( updateRequestWord.getSynonyms());
        currentWord.setAntonyms( updateRequestWord.getAntonyms());
        currentWord.setExtraMetaValue(REQUEST_MERGED, request.getRequestId());
        currentWord.setExtraMetaValue(request.getRequestId(), currentWordBackup.toString() );

        wordDao.updateWord(currentWord); //update the entry in DB

        saveRequestAsMerged(request); //Update the request as merged
        wordCache.cacheWord(currentWord);
        return currentWord;
    }

    private Word modifyToDeactivatedWordEntry(Word currentWordCopy) {

        //keeping the old word in the current words extra meta map with requestId
        currentWordCopy.setId( generateNewWordId() );
        EntityMeta currentCopyMeta = currentWordCopy.getEntityMeta();

        String validatorId = "validatorId";
        currentCopyMeta.setValidatorId(validatorId);

        String currentWordId = currentWordCopy.getId();
        currentCopyMeta.setParentId(currentWordId);
        currentCopyMeta.setStatus(EntityStatus.DEACTIVE);
        String deactivationDateString = (new DateTime(DateTimeZone.UTC)).toString();
        currentCopyMeta.setDeactivationDate(deactivationDateString); //marked it as de-active

        return currentWordCopy;
    }

    private void saveRequestAsMerged(UserRequest request) {

        String validatorId = "validatorId";
        EntityMeta requestMeta = request.getEntityMeta();
        requestMeta.setValidatorId(validatorId);
        requestMeta.setStatus(EntityStatus.DEACTIVE); //marked it as de-active
        String deactivationDateString = (new DateTime(DateTimeZone.UTC)).toString();
        requestMeta.setDeactivationDate(deactivationDateString);
        wordDao.updateRequest(request);
    }

    private Word approveDeleteWordRequest(UserRequest request){

        return null;
    }

    public static Word deepCopyWord(Word word) {
        return (Word) JsonUtil.jsonNodeToObject(JsonUtil.objectToJsonNode(word), Word.class);
    }

    public static String generateNewWordUpdateReqID() {
        return Constants.REQ_ID_PREFIX + "-" + UUID.randomUUID();
    }

    /* Delete Word */

    public void deleteWord(String wordId) {
        Word word = getWordByWordId(wordId);
        word.getEntityMeta().setStatus(EntityStatus.DEACTIVE);
        wordDao.updateWord(word);
        wordCache.uncacheWord(word);
    }

    /* LIST words todo */
    public ArrayList<Word> listWords(String startWordId, Integer limit) {
        return new ArrayList<>();
    }

    /* SEARCH words by a search string
     Cache all the spellings together for search!!
     Check if there is are ways to search by string on the indexed string, it should be very basic!
     ** You may return a smart object that specifies each close words and also suggestion if it didn't match
     How to find closest neighbour of a BanglaUtil word? you may be able to do that locally?
     */
    public Set<String> searchWords(String searchSting) {
        return searchWords(searchSting, Constants.SEARCH_SPELLING_LIMIT);
    }

    public Set<String> searchWords(String searchString, int limit){

        if(searchString == null || searchString.trim().length() == 0)
            return new HashSet<>();

        Set<String> words = wordCache.getWordsForSearchString(searchString);

        if(words != null && words.size() > 0)
            return words;

        bmLog.start();
        words = wordDao.searchWordSpellingsWithPrefixMatch(searchString, limit);

        if ( words != null && words.size() > 0 ) {

            bmLog.end("@WL002 search result [size:" + words.size() + "] for spelling:\"" + searchString + "\" found in database and returning");
            wordCache.cacheWordsForSearchString(searchString, words);
            return words;
        }

        bmLog.end("@WL003 search result for spelling:\"" + searchString + "\" not found in database");
        return new HashSet<>();
    }

    public long totalWordCount(){
        return wordDao.totalWordCount();
    }

    public void deleteAllWords(){
        wordDao.deleteAllWords();
    }

    public void flushCache(){
        wordCache.flushCache();
    }

    public static Word copyToNewWordObject(Word providedWord) {

        if(providedWord == null)
            return null;

        Word toReturnWord = new Word();

        if(providedWord.getWordSpelling() != null)
            toReturnWord.setWordSpelling(providedWord.getWordSpelling());

        if(providedWord.getExtraMetaMap() != null)
            toReturnWord.setExtraMetaMap( providedWord.getExtraMetaMap() );

        return toReturnWord;
    }


    /** CRUDL for meaning of a word:
     * most of the operations will deal with getting the word
     * and modifying the word to incorporate the create, update
     * and delete changes of a meaning and updating the word back
     * to  DB. The update should take into account of caching, i.e.
     * invalidating and re-caching the changes.
     */

    /* CREATE meaning */
    public JsonNode createMeaningJNode(String wordId, JsonNode meaningJsonNode) {
        Meaning meaning = (Meaning) JsonUtil.jsonNodeToObject(meaningJsonNode, Meaning.class);
        return convertMeaningToResponseJNode( createMeaning(wordId, meaning) );
    }

    public static JsonNode convertMeaningToResponseJNode(Meaning meaning) {

        JsonNode jsonNode = Json.toJson(meaning);
        List attributesToRemove = Arrays.asList("strength", "entityMeta");
        return JsonUtil.removeFieldsFromJsonNode(jsonNode, attributesToRemove);
    }

    public void createMeaningsBatch(String wordId, Collection<Meaning> meanings) {

        for (Meaning meaning: meanings) {
            createMeaning(wordId,meaning);
        }
    }

    public Meaning createMeaning(String wordId, Meaning meaning) {

        if(meaning.getMeaningId() != null)
            throw new IllegalArgumentException(Constants.CREATE_ID_NOT_PERMITTED + meaning.getMeaningId());

        if(meaning.getMeaning() == null || meaning.getMeaning().trim().length() == 0)
            throw new IllegalArgumentException(Constants.WORDSPELLING_NULLOREMPTY);

        Word currentWord = getWordByWordId(wordId);

        if(currentWord == null)
            throw new IllegalArgumentException(Constants.ENTITY_NOT_FOUND + wordId);

        String meaningId = generateNewMeaningId();
        meaning.setMeaningId( meaningId );
        WordHelper.addMeaningToWord(currentWord, meaning);

        wordDao.updateWord(currentWord);
        return meaning;
    }



    public static String generateNewMeaningId() {
        return Constants.MEANING_ID_PREFIX + UUID.randomUUID();
    }

    /* GET meaning */
    public JsonNode getMeaningJsonNodeByMeaningId(String wordId, String meaningId) {

        Meaning meaning = getMeaning(wordId, meaningId);
        return meaning == null? null: convertMeaningToResponseJNode(meaning);
    }

    public Meaning getMeaning(String wordId, String meaningId) {

        Word word = getWordByWordId(wordId);

        if(word == null || word.getMeaningsMap() == null)
            return null;

        return word.getMeaningsMap().get(meaningId);
    }

    /* UPDATE meaning todo implement using WORD's interfaces */
    public JsonNode updateMeaningJsonNode(String wordId, JsonNode meaningJsonNode) {

        Meaning meaning = (Meaning) JsonUtil.jsonNodeToObject(meaningJsonNode, Meaning.class);
        return convertMeaningToResponseJNode(updateMeaning(wordId, meaning));
    }

    public Meaning updateMeaning(String wordId, Meaning meaning) {

        if(meaning.getMeaningId() != null)
            throw new IllegalArgumentException(Constants.CREATE_ID_NOT_PERMITTED + meaning.getMeaningId());

        if(meaning.getMeaning() == null || meaning.getMeaning().trim().length() == 0)
            throw new IllegalArgumentException(Constants.WORDSPELLING_NULLOREMPTY);

        Word currentWord = getWordByWordId(wordId);

        if(currentWord == null)
            throw new IllegalArgumentException(Constants.ENTITY_NOT_FOUND + wordId);

        Meaning currentMeaning = currentWord.getMeaningsMap().get(meaning.getMeaningId());

        if(currentMeaning == null)
            throw new IllegalArgumentException(Constants.ENTITY_NOT_FOUND + meaning.getMeaningId());

        currentWord.getMeaningsMap().put(meaning.getMeaningId(), meaning);

        return meaning;
    }

    /* DELETE meaning todo implement using WORD's interfaces */
    public boolean deleteMeaning(String wordId, String meaningId) {

        Word word = getWordByWordId(wordId);

        if(word == null)
            throw new IllegalArgumentException("No word exists for id:" + wordId);

        if( word.getMeaningsMap() == null || word.getMeaningsMap().size() == 0)
            throw new IllegalArgumentException("Word does not have any meaning with meaningId:" + meaningId);

        return false;
    }

    /* LIST meaning todo implement using WORD's interfaces */
    public ArrayList<Meaning> listMeanings(String wordId) {

        return new ArrayList<>();
    }


}
