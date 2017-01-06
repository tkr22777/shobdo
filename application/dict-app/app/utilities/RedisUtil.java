package utilities;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by tahsink on 1/6/17.
 */
public class RedisUtil {

    public static String buildRedisKey(Collection<Object> keyParams) {

        Collection<String> paramToString = keyParams.stream().map( t -> t.toString() ).collect(Collectors.toList());

        return String.join("_", paramToString);
    }
}
