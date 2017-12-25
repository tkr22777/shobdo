package objects;

import lombok.Data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tahsinkabir on 8/21/16.
 */
@Data
public class PartsOfSpeechSet {

    public Set<String> partsOfSpeeches = new HashSet<String>(
            Arrays.asList(
                    "বিশেষ্য",
                    "বিশেষণ",
                    "সর্বনাম",
                    "অব্যয়",
                    "ক্রিয়া")
    );
}
