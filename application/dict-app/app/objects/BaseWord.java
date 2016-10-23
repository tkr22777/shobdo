package objects;

import utilities.DictUtil;

import java.util.*;

/**
 * Created by tahsinkabir on 6/16/16.
 */
public class BaseWord {

    private String wordId;
    private String wordSpelling;
    private int timesSearched;
    private String linkToPronunciation;
    private Map<String,String> extraMetaMap; //extra meta map can be kept for info that is difficult to parse

    public BaseWord(){

    }

    public BaseWord(String wordId, String wordSpelling, int timesSearched, String linkToPronunciation,
                    Map<String,String> extraMetaMap)
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

    public Map<String,String> getExtraMetaMap() {
        return extraMetaMap;
    }

    public void setExtraMetaMap(Map<String,String> extraMetaMap) {
        this.extraMetaMap = extraMetaMap;
    }

    public void setExtraMetaValue(String key, String value, boolean overwriteIfValueExists){

        if(extraMetaMap == null) extraMetaMap = new HashMap<>();

        if( extraMetaMap.get(key) == null || overwriteIfValueExists ) {
            extraMetaMap.put(key, value);
        } else {
            setExtraMetaValue(key + DictUtil.randomInRange(0,9), value, overwriteIfValueExists);
        }
    }

    public String retrieveExtraMetaValueForKey(String key){

        if(extraMetaMap != null)
            return extraMetaMap.get(key);
        else
            return null;
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

        return customBaseWordToString();

    }

    public String customBaseWordToString(){

        return "Base Word {" +
                //"\n\n\t\t\t Word Id = '" + wordId + "'" +
                "\n\n\t\t\t Word Spelling = '" + wordSpelling + "'" +
                //"\n\n\t\t\t Times Searched = " + timesSearched +
                //"\n\n\t\t\t Link To Pronunciation = '" + linkToPronunciation + "'" +
                //"\n\n\t\t\t Extra Meta Map = " + metaMapString() + "\n" +
                "\n\n\t\t}";
    }

}
