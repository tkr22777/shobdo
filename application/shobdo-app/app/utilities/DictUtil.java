package utilities;

import java.util.Random;

public class DictUtil {

    private static final ShobdoLogger log = new ShobdoLogger(DictUtil.class);

    private DictUtil() {
    }

    public static int randIntInRange(final int lowest, final int highest) {
        return new Random().nextInt(highest - lowest + 1) + lowest;
    }
}
