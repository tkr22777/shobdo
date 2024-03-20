package word.objects;

import common.objects.APIEntity;
import lombok.*;
import utilities.JsonUtil;

import java.util.Set;

@Data
@Setter
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Meaning implements APIEntity {

    private String id;
    private String text;
    private String exampleSentence;
    private String partOfSpeech;
    private Set<String> antonyms;
    private Set<String> synonyms;

    private String pronunciation;
    private int strength;

    public static Meaning fromMeaning(final Meaning meaning) {
        return (Meaning) JsonUtil.jNodeToObject(JsonUtil.objectToJNode(meaning), Meaning.class);
    }

    public void addAntonym(final String antonym) {
        antonyms.add(antonym);
    };

    public void removeAntonym(final String antonym) {
        antonyms.remove(antonym);
    };

    public void addSynonym(final String synonym) {
        synonyms.add(synonym);
    };

    public void removeSynonym(final String synonym) {
        synonyms.add(synonym);
    };

    @Override
    public String toString() {
        return JsonUtil.objectToJString(this);
    }
}