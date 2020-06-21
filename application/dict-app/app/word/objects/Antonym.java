package word.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import common.objects.APIEntity;
import lombok.*;

import java.util.Objects;

@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Antonym implements APIEntity {

    private String spelling;

    //wordId of the word which has the antonym as its spelling
    @JsonIgnore //Ignore to not expose to API
    private String targetWordId;

    //determines how strong of an antonym it is
    private int strength;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Antonym antonym = (Antonym) o;
        return spelling.equals(antonym.spelling);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spelling);
    }
}
