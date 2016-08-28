package utilities;

import java.util.Random;

/**
 * Created by tahsinkabir on 8/27/16.
 */
public class Util {

    public static int randomInRange(int lowest, int highest){
        return new Random().nextInt( highest - lowest + 1) + lowest;
    }

}
