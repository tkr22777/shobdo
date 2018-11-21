package objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import java.util.*;

/**
 * Created by Tahsin Kabir on 8/21/16.
 */
@Data @Builder
public class Word extends EntityMeta {

    private String id;
    private String wordSpelling;
    private ArrayList<String> antonyms = new ArrayList<>(); //antonym wordIds, should it map to associated spelling?
    private ArrayList<String> synonyms = new ArrayList<>(); //synonym wordIds, should it map to associated spelling?
    private HashMap<String, Meaning> meaningsMap = new HashMap<>(); //Think of this as the document store for meanings
    private HashMap<String,List<String>> extraMetaMap = new HashMap<>();

    public Word() {}

    @Override
    public JsonNode toJson() {
        return null;
    }

    public Word(final String id, final String wordSpelling) {
        this.id = id;
        this.wordSpelling = wordSpelling;
    }

    public void setExtraMetaValue (final String key, final String value) {
        setExtraMetaValue(key, Arrays.asList(value));
    }

    public void setExtraMetaValue(final String key, final List<String> newValues) {
        List<String> values = extraMetaMap.get(key);
        if (values == null) {
            values = new ArrayList<>();
        }
        values.addAll(newValues);
        extraMetaMap.put(key, values);
    }

    public List<String> retrieveExtraMetaValuesForKey(final String key) {
        return extraMetaMap.get(key);
    }

    public void removeExtraMetaValueForKey(final String key) {
        removeExtraMetaValueForKeys(Arrays.asList(key));
    }

    public void removeExtraMetaValueForKeys(final Collection<String> keys) {
        keys.forEach(k-> extraMetaMap.remove(k));
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException jpe) {
            return "ERROR 101";
        }
    }
}
