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
    private HashMap<String,Meaning> meaningsMap = new HashMap<>();
    private HashMap<String,List<String>> extraMetaMap; //used for any extra keyed metadata of freaking Strings! What the hack were you thinkin'?

    private String creatorId;
    private Date creationDate;

    //For versioning of the meaningsMap
    private String status = Constants.ENTITIY_ACTIVE;
    private String parentMeaningId; //null for pioneer word
    private Date deletedDate;
    private List<Word> previousVersions = new ArrayList<>(); //only the latest version should have previous versions

    //V1.5 validation of updates
    private String validatorId; //if validatorId is present, then the meaning is validated

    //V2 attributes
    private int version;

    public Word() { }

    public Word( String wordId, String wordSpelling) {

        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
    }

    public Word(String wordId,
                String wordSpelling,
                HashMap<String,Meaning> meaningsMap,
                HashMap<String,List<String>> extraMeta) {

        this.wordId = wordId;
        this.wordSpelling = wordSpelling;
        this.extraMetaMap = extraMeta;
        this.meaningsMap = meaningsMap;
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

    public List<String> retrieveExtraMetaValuesForKey(String key) {

        return extraMetaMap.get(key);
    }

    public void removeExtraMetaValueForKey(String key) {

        removeExtraMetaValueForKeys(Arrays.asList(key));
    }

    public void removeExtraMetaValueForKeys(Collection<String> keys) {

        for(String key: keys) {
            extraMetaMap.remove(key);
        }
    }

    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }
}
