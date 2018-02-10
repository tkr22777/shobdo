package logics;

import cache.WordCache;
import com.fasterxml.jackson.databind.JsonNode;
import daoImplementation.WordDaoMongoImpl;
import daos.WordDao;
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
        return convertWordToResponseJNode(createdWord);
    }

    public static JsonNode convertWordToResponseJNode(Word word) {

        JsonNode jsonNode = Json.toJson(word);
        List attributesToRemove = Arrays.asList("extraMetaMap", "versionMeta");
        return JsonUtil.removeFieldsFromJsonNode(jsonNode, attributesToRemove);
    }

    public Word createWord(Word word) {

        if(word.getId() != null)
            throw new IllegalArgumentException(Constants.CREATE_ID_NOT_PERMITTED + word.getId());

        if(word.getWordSpelling() == null || word.getWordSpelling().trim().length() == 0)
            throw new IllegalArgumentException(Constants.WORDSPELLING_NULLOREMPTY);

        if(getWordBySpelling( word.getWordSpelling() ) != null)
            throw new IllegalArgumentException(Constants.CREATE_SPELLING_EXISTS + word.getWordSpelling());

        if(word.getMeaningsMap() != null && word.getMeaningsMap().size() > 0)
            throw new IllegalArgumentException(Constants.MEANING_PROVIDED);

        //todo get creatorId from context
        VersionMeta versionMeta = new VersionMeta();
        String creatorId =  "sin";
        versionMeta.setCreatorId( creatorId );
        versionMeta.setType(SOTypes.WORD);
        versionMeta.setStatus(SOStatus.ACTIVE);
        String creationDateString = (new DateTime(DateTimeZone.UTC)).toString();
        versionMeta.setCreationDate( creationDateString );

        word.setId( generateNewWordId() );
        word.setVersionMeta(versionMeta);

        wordDao.createWord(word);
        wordCache.cacheWord(word);

        Word wordByWordId = wordDao.getWordByWordId(word.getId());

        log.info("Word by wordId: " + wordByWordId);

        return word;
    }

    public static String generateNewWordId() {
        return Constants.WORD_ID_PREFIX + "-" + UUID.randomUUID();
    }

    public void createWordsBatch(Collection<Word> words) {

        for(Word word: words)
            createWord(word);
    }

    /* GET word by id */
    public JsonNode getWordJNodeByWordId(String wordId) {

        Word word = getWordByWordId(wordId);
        return word == null? null: convertWordToResponseJNode(word);
    }

    public Word getWordByWordId(String wordId) {

        if( wordId == null || wordId.trim().length() == 0 )
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY + wordId);

        return wordDao.getWordByWordId(wordId);
    }

    /* GET word by (exact) spelling */
    public JsonNode getWordJNodeBySpelling(String spelling) {

        Word word = getWordBySpelling(spelling);
        return word == null? null: convertWordToResponseJNode(word);
    }

    public Word getWordBySpelling(String spelling) {

        if(spelling == null || spelling == "")
            throw new IllegalArgumentException("WLEX: getWordBySpelling word spelling is null or empty");

        Word cachedWord = wordCache.getWordBySpelling(spelling);

        if(cachedWord != null)
            return cachedWord;

        Word wordFromDB = wordDao.getWordBySpelling(spelling);
        wordCache.cacheWord(wordFromDB);
        return wordFromDB;
    }

    /* Update */

    public JsonNode updateWordJNode(String wordId, JsonNode wordJsonNode) {

        Word word = (Word) JsonUtil.jsonNodeToObject(wordJsonNode, Word.class);
        word.setId(wordId);
        Word updatedWord = updateWordVersioned(word);
        return convertWordToResponseJNode(updatedWord);
    }

    public Word updateWordVersioned(Word updateWord) {

        try {

            String requestId = createUpdateWordRequest(updateWord);
            return approveWordRequest( requestId );

        } catch (Exception ex) {

            if(ex instanceof  IllegalArgumentException)
                throw ex;

            //ex.printStackTrace(System.out);
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
        Word currentWord = getWordByWordId(currentWordId);

        if(currentWord == null)
            throw new IllegalArgumentException(Constants.ENTITY_NOT_FOUND + updateWord.getId());

        VersionMeta currentVersion = currentWord.getVersionMeta();

        if(currentVersion.getStatus().equals(SOStatus.LOCKED))
            throw new IllegalArgumentException(Constants.ENTITY_LOCKED + updateWord.getId());

        //Create a SRequest object for the word
        String requestId = generateNewWordUpdateReqID();

        SRequest updateRequest = new SRequest();
        updateRequest.setRequestId(requestId);
        updateRequest.setTargetId(currentWordId);
        updateRequest.setTargetType(SOTypes.WORD);
        updateRequest.setOperation(SROperation.UPDATE);
        updateRequest.setBody(JsonUtil.objectToJsonNode(updateWord));

        VersionMeta requestVersion = new VersionMeta();
        String creatorId =  "sin";
        requestVersion.setCreatorId(creatorId);
        requestVersion.setType(SOTypes.REQUEST);
        requestVersion.setStatus(SOStatus.ACTIVE);
        String creationDateString = (new DateTime(DateTimeZone.UTC)).toString();
        requestVersion.setCreationDate(creationDateString);

        updateRequest.setVersionMeta(requestVersion);

        wordDao.createRequest(updateRequest);

        return requestId;
    }

    //todo make transactional
    //approveWordRequest applies the requested changes to a word
    public Word approveWordRequest(String requestId) {

        if(requestId == null || requestId.trim().length() == 0)
            throw new IllegalArgumentException(Constants.ID_NULLOREMPTY);

        SRequest storedRequest = wordDao.getRequestById(requestId);

        if( storedRequest == null )
            throw new IllegalArgumentException( Constants.ENTITY_NOT_FOUND + requestId );

        if( !storedRequest.getVersionMeta().getStatus().equals(SOStatus.ACTIVE) )
            throw new IllegalArgumentException( Constants.ENTITY_IS_DEACTIVE + requestId );

        switch (storedRequest.getOperation()) {

            case SROperation.CREATE:
                return approveCreateWordRequest(storedRequest);
            case SROperation.UPDATE:
                return approveUpdateWordRequest(storedRequest);
            case SROperation.DELETE:
                return approveDeleteWordRequest(storedRequest);
            default:
                return null;
        }
    }

    private Word approveCreateWordRequest(SRequest request){

        return null;
    }

    private Word approveUpdateWordRequest(SRequest request){

        String validatorId = "validatorId";
        String currentWordId = request.getTargetId();

        Word currentWord = wordDao.getWordByWordId(currentWordId);
        Word currentWordCopy = deepCopyWord(currentWord);

        //updated the word with the updates
        JsonNode updateWordBody = request.getBody();
        Word updateRequestWord = (Word) JsonUtil.jsonNodeToObject( updateWordBody, Word.class);
        currentWord.setWordSpelling( updateRequestWord.getWordSpelling() );
        currentWord.setSynonyms( updateRequestWord.getSynonyms());
        currentWord.setAntonyms( updateRequestWord.getAntonyms());
        currentWord.setExtraMetaValue(REQUEST_MERGED, request.getRequestId());

        //keeping the old word in the current words extra meta map with requestId
        currentWordCopy.setId( generateNewWordId() );
        VersionMeta currentCopyVersion = currentWordCopy.getVersionMeta();
        currentCopyVersion.setParentId(currentWordId);
        currentCopyVersion.setStatus(SOStatus.UPDATED);
        String deactivationDateString = (new DateTime(DateTimeZone.UTC)).toString();
        currentCopyVersion.setDeactivationDate(deactivationDateString); //marked it as de-active
        currentCopyVersion.setValidatorId(validatorId);
        currentWord.setExtraMetaValue(request.getRequestId(), currentWordCopy.toString() );

        wordDao.updateWord(currentWord);

        //updated the request as merged
        VersionMeta requestVersion = request.getVersionMeta();
        requestVersion.setStatus(SOStatus.MERGED);
        requestVersion.setDeactivationDate(deactivationDateString); //marked it as de-active
        requestVersion.setValidatorId(validatorId);
        log.info("Updated entry of request:" + request);
        wordDao.updateRequest(request);

        return currentWord;
    }

    private Word approveDeleteWordRequest(SRequest request){

        return null;
    }

    public static Word deepCopyWord(Word word) {
        return (Word) JsonUtil.jsonNodeToObject(JsonUtil.objectToJsonNode(word), Word.class);
    }

    public static String generateNewWordUpdateReqID() {
        return Constants.REQ_ID_PREFIX + "-" + UUID.randomUUID();
    }

    /* Delete to do */

    public void deleteWord(String wordId) {
        wordDao.deleteWord(wordId);
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
        words = wordDao.getWordSpellingsWithPrefixMatch(searchString, limit);

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
        List attributesToRemove = Arrays.asList("strength", "versionMeta");
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
        addMeaningToWord(currentWord, meaning);

        wordDao.updateWord(currentWord);
        return meaning;
    }

    private Word addMeaningToWord(Word word, Meaning meaning) {

        if(word == null || meaning == null || meaning.getMeaningId() == null)
            throw new RuntimeException("Temp");

        HashMap<String, Meaning> meaningHashMap = word.getMeaningsMap();
        if(meaningHashMap == null)
            meaningHashMap = new HashMap<>();
        meaningHashMap.put(meaning.getMeaningId(), meaning);
        word.setMeaningsMap(meaningHashMap);
        return  word;
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

        addMeaningToWord(currentWord, meaning);

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
