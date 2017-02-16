package objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import utilities.Constants;
import utilities.DictUtil;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseWord baseWord = (BaseWord) o;

        return wordSpelling != null ? wordSpelling.equals(baseWord.wordSpelling) : baseWord.wordSpelling == null;
    }

    @Override
    public int hashCode() {
        return wordSpelling != null ? wordSpelling.hashCode() : 0;
    }

    public String metaMapString(){

        String toReturn = " {\n";

        int i = 1;
        for(String key: this.extraMetaMap.keySet() ){

            toReturn += "\n\t\t\t\tKey "+i + ": '"+ key + "'" +
                        "\n\t\t\t\tValue:'" + extraMetaMap.get(key) + "'\n";
            i++;
        }

        toReturn += "\t\t\t}";

        return  toReturn;
    }

    @Override
    public String toString() {

        if(Constants.CUSTOM_STRING)
            return customToStringBaseWord();
        else
            return toJsonString();
    }

    public String toJsonString() {

        String jsonString = null;

        try {

            jsonString = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);

        } catch (JsonProcessingException exception) {

            LogPrint log = new LogPrint(BaseWord.class);
            log.info("BW001: Json Processing Exception Message: " + exception.getMessage());
        }

        return jsonString;
    }

    public String customToStringBaseWord() {

        return "Base Word {" +
                //"\n\n\t\t\t Word Id = '" + wordId + "'" +
                "\n\n\t\t\t Word Spelling = '" + wordSpelling + "'" +
                //"\n\n\t\t\t Times Searched = " + timesSearched +
                //"\n\n\t\t\t Link To Pronunciation = '" + linkToPronunciation + "'" +
                //"\n\n\t\t\t Extra Meta Map = " + metaMapString() + "\n" +
                "\n\n\t\t}";
    }

}
