package objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import play.api.libs.json.Json;
import utilities.Constants;
import utilities.JsonUtil;
import utilities.LogPrint;

import java.util.*;

/**
 * Created by tahsinkabir on 8/21/16.
 */
public class DictionaryWord {

    //arrangementType is the way the meanings of this word are arranged now
    //e.g. by parts of speech or by strength of the meaning (are not arranged by parts of speech, such as V, N , N , P, V)
    //The following array list can be represented as (arranged by meaning strength):
    //{ { V { A1, A2 } } , { N { A3 } }, { N { A4, A5} } , { V { A6 } } }
    //or as (arranged by combined strength for each of the parts of speeches and words ordered within them):
    //{ { V { A1, A2, A6 } } , { N { A3, A4, A5 } } } //<-- lets only support this

    private int version;
    private boolean reviewed;
    private int timesSearched;
    private boolean diplayToPublic;
    private String linkToPronunciation;
    private ArrayList<String> otherSpellings; //list of other correct or incorrect very similar spellings for the word
    private Map<String,List<String>> extraMetaMap; //used for any extra keyed metadata

    private String wordId;
    private String wordSpelling;
    ArrayList<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches;

    String arrangementType = null; //Arrangement partsOfSpeech will be used in V2

    public DictionaryWord() { }

    public DictionaryWord(String wordId,
                          String wordSpelling)
    {
        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
    }

    public DictionaryWord(String wordId,
                          String wordSpelling,
                          String arrangementType,
                          ArrayList<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches)
    {

        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
        this.arrangementType = arrangementType;
        this.meaningForPartsOfSpeeches = meaningForPartsOfSpeeches;
    }

    public DictionaryWord(String wordId,
                          String wordSpelling,
                          int timesSearched,
                          String linkToPronunciation,
                          String arrangementType,
                          Collection<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches,
                          Map<String,List<String>> extraMeta)
    {
        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
        this.timesSearched = timesSearched;
        this.linkToPronunciation = linkToPronunciation;
        this.extraMetaMap = extraMeta;
        this.arrangementType = arrangementType;
        this.meaningForPartsOfSpeeches = new ArrayList<>(meaningForPartsOfSpeeches);
    }

    public static DictionaryWord wordFromJsonString(String wordInJsonString) {

        return (DictionaryWord) JsonUtil.toObjectFromJsonString(wordInJsonString,DictionaryWord.class);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public boolean isDiplayToPublic() {
        return diplayToPublic;
    }

    public void setDiplayToPublic(boolean diplayToPublic) {
        this.diplayToPublic = diplayToPublic;
    }

    public String getArrangementType() {
        return arrangementType;
    }

    public void setArrangementType(String arrangementType) {
        this.arrangementType = arrangementType;
    }

    public ArrayList<MeaningForPartsOfSpeech> getMeaningForPartsOfSpeeches() {
        return meaningForPartsOfSpeeches;
    }

    public void setMeaningForPartsOfSpeeches(ArrayList<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches) {
        this.meaningForPartsOfSpeeches = meaningForPartsOfSpeeches;
    }

    public void addMeaningForPartsOfSpeech(MeaningForPartsOfSpeech aMeaningForPartsOfSpeech) {

        if(meaningForPartsOfSpeeches == null)
            meaningForPartsOfSpeeches = new ArrayList<>();

        meaningForPartsOfSpeeches.add(aMeaningForPartsOfSpeech);
    }
     public ArrayList<String> getOtherSpellings() {
        return otherSpellings;
    }

    public void setOtherSpellings(ArrayList<String> otherSpellings) {
        this.otherSpellings = otherSpellings;
    }

    public String getWordId() {
        return wordId;
    }

    public void setWordId(String wordId) {
        this.wordId = wordId;
    }

    public String getWordSpelling() {
        return wordSpelling;
    }

    public void setWordSpelling(String wordSpelling) {
        this.wordSpelling = wordSpelling;
    }

    public int getTimesSearched() {
        return timesSearched;
    }

    public void setTimesSearched(int timesSearched) {
        this.timesSearched = timesSearched;
    }

    public Map<String,List<String>> getExtraMetaMap() {
        return extraMetaMap;
    }

    public void setExtraMetaMap( Map<String,List<String>>  map) {
        this.extraMetaMap = map;
    }

    public void setExtraMetaValue(String key, String value){

        setExtraMetaValue(key, Arrays.asList(value));
    }

    public void setExtraMetaValue(String key, List<String> newValues){

        if(extraMetaMap == null)
            extraMetaMap = new HashMap<>();

        List<String> values = extraMetaMap.get(key);

        if( values == null )
            values = new ArrayList<>();

        values.addAll(newValues);

        extraMetaMap.put(key, values);
    }

    public List<String> retrieveExtraMetaValueForKey(String key){

        return extraMetaMap.get(key);
    }

    public void removeExtraMetaValueForKey(String key){

        removeExtraMetaValueForKeys(Arrays.asList(key));
    }

    public void removeExtraMetaValueForKeys(Collection<String> keys){

        for(String key: keys) {
            extraMetaMap.remove(key);
        }
    }

    public Set<String> retriveExtraMetaKeys(){

        return this.extraMetaMap.keySet();
    }

    public String getLinkToPronunciation() {
        return linkToPronunciation;
    }

    public void setLinkToPronunciation(String linkToPronunciation) {
        this.linkToPronunciation = linkToPronunciation;
    }

    @Override
    public String toString() {

        if(Constants.JSON_STRING)
            return toJsonString();
        else
            return "DictionaryWord{" +
                    "version=" + version +
                    ", reviewed=" + reviewed +
                    ", diplayToPublic=" + diplayToPublic +
                    ", arrangementType='" + arrangementType + '\'' +
                    ", meaningForPartsOfSpeeches=" + meaningForPartsOfSpeeches +
                    '}';
    }

    public String toJsonString() {

        return JsonUtil.toJsonString(this);
    }
}
