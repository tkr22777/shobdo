package word.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.util.Objects;

@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Synonym {

    private String spelling;

    //wordId of the word which has the synonym as its spelling
    @JsonIgnore  //Ignore to not expose to API
    private String targetWordId;

    //determines how strong of an antonym it is
    private int strength;

    public JsonNode toAPIJsonNode() {
        return new ObjectMapper().convertValue(this, JsonNode.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Synonym synonym = (Synonym) o;
        return spelling.equals(synonym.spelling);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spelling);
    }
}
