package objects;

import lombok.Data;
import utilities.Constants;
import utilities.JsonUtil;

import java.util.*;

/**
 * Created by tahsinkabir on 8/21/16.
 */
@Data
public class Word {

    private String wordId;
    private String wordSpelling;
    ArrayList<Meaning> meanings;
    private Map<String,List<String>> extraMetaMap; //used for any extra keyed metadata of freaking Strings! What the hack were you thinkin'?
    private Date deletedDate;

    int version; //remove it?

    public Word() { }

    public Word( String wordId, String wordSpelling) {

        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
    }

    public Word(String wordId,
                String wordSpelling,
                Collection<Meaning> meanings,
                Map<String,List<String>> extraMeta)
    {
        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
        this.extraMetaMap = extraMeta;
        this.meanings = new ArrayList<>(meanings);
    }

    public void addMeaningForPartsOfSpeech(Meaning meaning) {

        if(meanings == null)
            meanings = new ArrayList<>();

        meanings.add(meaning);
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

    public Set<String> retrieveExtraMetaKeys(){

        return this.extraMetaMap.keySet();
    }

    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}
