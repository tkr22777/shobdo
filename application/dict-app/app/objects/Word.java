package objects;

import utilities.Constants;
import utilities.JsonUtil;

import java.util.*;

/**
 * Created by tahsinkabir on 8/21/16.
 */
public class Word {

    //arrangementType is the way the meanings of this word are arranged now
    //e.g. by parts of speech or by strength of the meanings (are not arranged by parts of speech, such as V, N , N , P, V)
    //The following array list can be represented as (arranged by meanings strength):
    //{ { V { A1, A2 } } , { N { A3 } }, { N { A4, A5} } , { V { A6 } } }
    //or as (arranged by combined strength for each of the parts of speeches and words ordered within them):
    //{ { V { A1, A2, A6 } } , { N { A3, A4, A5 } } } //<-- lets only support this

    private int version;
    private int timesSearched;
    private Map<String,List<String>> extraMetaMap; //used for any extra keyed metadata

    private String wordId;
    private String wordSpelling;
    ArrayList<Meaning> meanings;

    public Word() { }

    public Word(String wordId,
                String wordSpelling)
    {
        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
    }

    public Word(String wordId,
                String wordSpelling,
                ArrayList<Meaning> meanings)
    {

        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
        this.meanings = meanings;
    }

    public Word(String wordId,
                String wordSpelling,
                int timesSearched,
                String linkToPronunciation,
                Collection<Meaning> meanings,
                Map<String,List<String>> extraMeta)
    {
        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
        this.timesSearched = timesSearched;
        this.extraMetaMap = extraMeta;
        this.meanings = new ArrayList<>(meanings);
    }

    public static Word wordFromJsonString(String wordInJsonString) {

        return (Word) JsonUtil.toObjectFromJsonString(wordInJsonString, Word.class);
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public ArrayList<Meaning> getMeanings() {
        return meanings;
    }

    public void setMeanings(ArrayList<Meaning> meanings) {
        this.meanings = meanings;
    }

    public void addMeaningForPartsOfSpeech(Meaning meaning) {

        if(meanings == null)
            meanings = new ArrayList<>();

        meanings.add(meaning);
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

    @Override
    public String toString() {

        return toJsonString();
    }

    public String toJsonString() {

        return JsonUtil.toJsonString(this);
    }
}
