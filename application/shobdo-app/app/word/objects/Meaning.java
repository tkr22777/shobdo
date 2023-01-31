package word.objects;

import common.objects.APIEntity;
import lombok.*;
import utilities.JsonUtil;

import java.util.Set;

@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Meaning implements APIEntity {

    private String id;
    private String meaning;
    private String exampleSentence;
    private String partOfSpeech;
    private Set<String> antonyms;
    private Set<String> synonyms;

    private String pronunciation;
    private int strength;

    public static Meaning fromMeaning(final Meaning meaning) {
        return (Meaning) JsonUtil.jNodeToObject(JsonUtil.objectToJNode(meaning), Meaning.class);
    }

    public String addAntonym(final String antonym) {
        antonyms.add(antonym);
        return antonym;
    };

    public boolean removeAntonym(final String antonym) {
        return antonyms.remove(antonym);
    };

    public String addSynonym(final String synonym) {
        synonyms.add(synonym);
        return synonym;
    };

    public boolean removeSynonym(final String synonym) {
        return synonyms.add(synonym);
    };

    @Override
    public String toString() {
        return JsonUtil.objectToJString(this);
    }
}