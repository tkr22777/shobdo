package logics;

import cache.WordCache;
import com.fasterxml.jackson.databind.JsonNode;
import daoImplementation.WordDaoMongoImpl;
import daos.WordDao;
import objects.Meaning;
import objects.VersionMeta;
import objects.Word;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import play.libs.Json;
import utilities.BenchmarkLogger;
import utilities.Constants;
import utilities.JsonUtil;
import utilities.LogPrint;

import java.util.*;

/**
 * Created by tahsinkabir on 8/14/16.
 */
public class WordLogic {

    private WordDao wordDao;
    private WordCache wordCache;

    private BenchmarkLogger bmLog = new BenchmarkLogger(WordLogic.class);
    private LogPrint log = new LogPrint(WordLogic.class);

    public static WordLogic factory() {

        WordDao wordDao = new WordDaoMongoImpl();
        return new WordLogic( wordDao, new WordCache() );
    }

    public WordLogic( WordDao wordDao, WordCache wordCache) {

        this.wordDao = wordDao;
        this.wordCache = wordCache;
    }

    /* Start of CRUDLS of WORD objects in the logic class */

    /* CREATE word(s)
        * wordId should always be generated here (createWord of wordLogic)
        * no meaning creation is allowed with the create word endpoint for now */
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

        if(word.getWordId() != null)
            throw new IllegalArgumentException(Constants.CREATE_WORDID_EXISTS + word.getWordId());

        if(word.getWordSpelling() == null || "".equalsIgnoreCase(word.getWordSpelling()))
            throw new IllegalArgumentException(Constants.CREATE_SPELLING_NOT_PROVIDED);

        if(getWordBySpelling( word.getWordSpelling() ) != null)
            throw new IllegalArgumentException(Constants.CREATE_SPELLING_EXISTS + word.getWordSpelling());

        if(word.getMeaningsMap() != null && word.getMeaningsMap().size() > 0)
            throw new IllegalArgumentException(Constants.CREATE_MEANING_PROVIDED);

        word.setWordId( generateNewWordId() );

        //todo get creatorId from context
        String creatorId =  "sin";
        VersionMeta versionMeta = new VersionMeta();
        versionMeta.setCreatorId( creatorId );
        versionMeta.setStatus(Constants.ENTITIY_ACTIVE);
        String creationDateString = (new DateTime(DateTimeZone.UTC)).toString();
        versionMeta.setCreationDate( creationDateString );

        word.setVersionMeta(versionMeta);

        wordDao.createWord(word);
        wordCache.cacheWord(word);

        return word;
    }

    public static String generateNewWordId() {
        return Constants.WORD_ID_PREFIX + "-" + UUID.randomUUID();
    }

    public void createWordsBatch(Collection<Word> words) {

        for(Word word: words)
            createWord(word);
    }

    /* GET word by wordId */
    public JsonNode getWordJNodeByWordId(String wordId) {

        Word word = getWordByWordId(wordId);
        return word == null? null: convertWordToResponseJNode(word);
    }

    public Word getWordByWordId(String wordId) {

        if( wordId == null || "".equalsIgnoreCase(wordId) )
            throw new IllegalArgumentException(Constants.GET_WORDID_NULLEMPTY + wordId);

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

    /* UPDATE word by wordId todo

       We will version each updates:

       Versioned update pseudo-code:
           We will copy the current object, copyWord
           On copyWord,
               set a new wordId, //since we do not want to destruct the existing wordId for the client
               set a deletedDate,
               set status as UPDATED
               set the meaningsMap to null //we will keep the meaningsMap on the word being updated
               set the previousVersions to null //we will keep the previousVersions on the word being updated
               set the extraMetaMap to null //we will keep the extraMetaMap on the word being updated
           On the actual object,
               update data,
               set the parentWordId to the wordId of the copyWord
               add the copyWord to previousVersions
    */

    public void updateWord(String wordId, Word word) {

        if(wordId == null)
            throw new IllegalArgumentException("wordId is null, cannot update");

        if(word.getWordId() == null)
            word.setWordId(wordId);

        if(word.getWordId().equals(wordId))
            throw new IllegalArgumentException("wordId on route do not match");

        updateWord(word);
    }

    public void updateWord(Word word) {

        verifyWordForUpsert(word);
    }

    /* DELETE word by wordId todo
       For a delete:
           Set the deletedDate
           Set status as DELETED
    */

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

        if(searchString == null || searchString.equals(""))
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

        Word toReturnWord = new Word();

        toReturnWord.setWordId(null);

        if(providedWord != null) {

            if(providedWord.getWordSpelling() != null)
                toReturnWord.setWordSpelling(providedWord.getWordSpelling());

            if(providedWord.getExtraMetaMap() != null)
                toReturnWord.setExtraMetaMap( providedWord.getExtraMetaMap() );
        }

        return toReturnWord;
    }

    private void verifyWordForUpsert(Word word) {

        if(word.getWordId() == null) {
            log.info("@WL004 wordId is null");
            throw new IllegalArgumentException("wordId is null");
        }

        if(word.getWordSpelling() == null || word.getWordSpelling().trim().length() == 0) {
            log.info("@WL005 wordSpelling is null or empty");
            throw new IllegalArgumentException("word spelling cannot be null or empty");
        }

        if(word.getMeaningsMap() == null || word.getMeaningsMap().size() == 0) {
            log.info("@WL006 meaningsMap array is null or empty");
            throw new IllegalArgumentException("word meaning cannot be null or empty");
        }
    }

    /** CRUDL for meaning of a word:
     * most of the operations will deal with getting the word
     * and modifying the word to incorporate the create, update
     * and delete changes of a meaning and updating the word back
     * to  DB. The update should take into account of caching, i.e.
     * invalidating and re-caching the changes.
     */

    /* CREATE meaning todo implement using WORD's interfaces */
    public String createMeaning(String wordId, Meaning meaning) {
        Word word = getWordByWordId(wordId);
        //add the meaning to the word and save
        return "newMeaningId";
    }

    public static String generateNewMeaningId() {
        return Constants.MEANING_ID_PREFIX + UUID.randomUUID();
    }

    /* GET meaning todo implement using WORD's interfaces */
    public Meaning getMeaning(String wordId, String meaningId) {

        Word word = getWordByWordId(wordId);
        if(word == null || word.getMeaningsMap() == null)
            return null;

        return word.getMeaningsMap().get(meaningId);
    }

    /* UPDATE meaning todo implement using WORD's interfaces */
    public boolean updateMeaning(String wordId, Meaning meaning) {

        return false;
    }

    /* DELETE meaning todo implement using WORD's interfaces */
    public boolean deleteMeaning(String wordId, String meaningId) {

        Word word = getWordByWordId(wordId);

        if(word == null)
            throw new IllegalArgumentException("No word exists for wordId:" + wordId);

        if( word.getMeaningsMap() == null || word.getMeaningsMap().size() == 0)
            throw new IllegalArgumentException("Word does not have any meaning with meaningId:" + meaningId);

        return false;
    }

    /* LIST meaning todo implement using WORD's interfaces */
    public ArrayList<Meaning> listMeanings(String wordId) {

        return new ArrayList<>();
    }


}
