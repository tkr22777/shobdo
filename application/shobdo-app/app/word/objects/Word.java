package word.objects;

import common.objects.MongoEntity;
import common.objects.APIEntity;
import lombok.*;
import org.bson.Document;
import utilities.JsonUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class Word extends MongoEntity implements APIEntity {

    private String id;
    private String spelling;
    private Set<Antonym> antonyms;
    private Set<Synonym> synonyms;

    //Map of meaningId to meaning objects for the the word, easier to lookup to following operations
    private HashMap<String, Meaning> meanings;

    /* Should move these logic to word logic? */
    public void addMeaningToWord(final Meaning meaning) {
        if (meaning == null || meaning.getId() == null) {
            throw new RuntimeException("Meaning or MeaningId is null");
        }
        if (meanings == null) {
            meanings = new HashMap<>();
        }
        getMeanings().put(meaning.getId(), meaning);
    }

    public void addAntonym(final Antonym antonym) {
        if (this.antonyms == null) {
            this.antonyms = new HashSet<>();
        }
        antonyms.add(antonym);
    }

    public void removeAntonym(final Antonym antonym) {
        if (this.antonyms == null) {
            this.antonyms = new HashSet<>();
        }
        antonyms.remove(antonym);
    }

    public void addSynonym(final Synonym synonym) {
        if (this.synonyms == null) {
            this.synonyms = new HashSet<>();
        }
        synonyms.add(synonym);
    }

    public void removeSynonym(final Synonym synonym) {
        if (this.synonyms == null) {
            this.synonyms = new HashSet<>();
        }
        synonyms.remove(synonym);
    }

    public static Word fromBsonDoc(final Document doc) {
        doc.remove("_id");
        return (Word) JsonUtil.jStringToObject(doc.toJson(), Word.class);
    }

    public static Word fromWord(final Word word) {
        return (Word) JsonUtil.jNodeToObject(JsonUtil.objectToJNode(word), Word.class);
    }

    @Override
    public String toString() {
        return JsonUtil.objectToJString(this);
    }
}
