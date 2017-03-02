package objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import utilities.Constants;
import utilities.DictUtil;
import utilities.JsonUtil;
import utilities.LogPrint;

import java.util.*;

/**
 * Created by tahsinkabir on 6/16/16.
 */
public class BaseWord {

    private String wordId;
    private String wordSpelling;
    private ArrayList<String> otherSpellings; //list of other correct or incorrect very similar spellings for the word
    private int timesSearched;
    private String linkToPronunciation;
    private Map<String,List<String>> extraMetaMap; //used for any extra keyed metadata

    public BaseWord(){

    }

    public BaseWord(String wordId, String wordSpelling, int timesSearched, String linkToPronunciation,
                    Map<String,List<String>> extraMetaMap)
    {
        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
        this.timesSearched = timesSearched;
        this.linkToPronunciation = linkToPronunciation;
        this.extraMetaMap = extraMetaMap;
    }

    public BaseWord(String wordId, String wordSpelling) {
        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
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
            return "BaseWord{" +
                    "wordId='" + wordId + '\'' +
                    ", wordSpelling='" + wordSpelling + '\'' +
                    ", otherSpellings=" + otherSpellings +
                    ", timesSearched=" + timesSearched +
                    ", linkToPronunciation='" + linkToPronunciation + '\'' +
                    ", extraMetaMap=" + extraMetaMap +
                    '}';
    }

    public String toJsonString() {

        return JsonUtil.toJsonString(this);
    }

}
