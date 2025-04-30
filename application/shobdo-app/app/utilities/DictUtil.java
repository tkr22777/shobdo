package utilities;

import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DictUtil {

    private static final Logger log = LoggerFactory.getLogger(DictUtil.class);

    private DictUtil() {
    }

    public static int randIntInRange(final int lowest, final int highest) {
        return new Random().nextInt(highest - lowest + 1) + lowest;
    }
}
