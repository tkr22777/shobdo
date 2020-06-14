package objects;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PartsOfSpeech {
    public Set<String> partsOfSpeeches = new HashSet<>(
        Arrays.asList(
            "বিশেষ্য",
            "বিশেষণ",
            "সর্বনাম",
            "অব্যয়",
            "ক্রিয়া")
    );
}
