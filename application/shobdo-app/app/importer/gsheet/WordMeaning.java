package importer.gsheet;

import lombok.*;
import word.objects.Meaning;
import word.objects.Word;

@Data
@Setter
@Getter
@Builder
@AllArgsConstructor
@ToString
public class WordMeaning {
    Word word;
    Meaning meaning;
}
