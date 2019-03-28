package objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;
import utilities.JsonUtil;

import java.util.*;

/**
 * Created by Tahsin Kabir on 8/21/16.
 */
@Data @Builder
public class Word extends EntityMeta {

    private String id;
    private String wordSpelling;

    //set of spellings of antonyms of the word
    private HashSet<String> antonyms;

    //set of spellings of synonyms of the word
    private HashSet<String> synonyms;

    //meaningId to meanings map, easier to lookup
    private final HashMap<String, Meaning> meaningsMap;

    public void addMeaningToWord(final Meaning meaning) {
        if (meaning == null || meaning.getId() == null) {
            throw new RuntimeException("Meaning or MeaningId is null");
        }
        getMeaningsMap().put(meaning.getId(), meaning);
    }

    public JsonNode toJson() {
        final JsonNode jsonNode = JsonUtil.objectToJsonNode(this);
        return JsonUtil.removeFieldsFromJsonNode(jsonNode, Arrays.asList("entityMeta", "others"));
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
