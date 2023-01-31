package importer.gsheet;

import lombok.*;
import word.objects.Meaning;

@Data
@Setter
@Getter
@Builder
@AllArgsConstructor
@ToString
public class WordMeaning {
    Meaning meaning;
    String word;
}
