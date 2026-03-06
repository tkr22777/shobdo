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
@EqualsAndHashCode(callSuper = false)
public class Inflection implements APIEntity {

    /** The inflected spelling, e.g. "আলোচনায়" */
    private String spelling;

    /** Grammatical type in Bangla, e.g. "অধিকরণ", "সম্বন্ধ", "বহুবচন" */
    private String type;

    /** Short meaning specific to this inflected form */
    private String meaning;

    private String exampleSentence;

    private Set<String> synonyms;

    private Set<String> antonyms;

    @Override
    public String toString() {
        return JsonUtil.objectToJString(this);
    }
}
