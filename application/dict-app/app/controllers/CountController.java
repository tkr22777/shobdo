package controllers;

import javax.inject.*;

import org.slf4j.LoggerFactory;
import play.*;
import play.mvc.*;
import redis.clients.jedis.Jedis;


import services.Counter;

/**
 * This controller demonstrates how to use dependency injection to
 * bind a component into a controller class. The class contains an
 * action that shows an incrementing count to users. The {@link Counter}
 * object is injected by the Guice dependency injection system.
 */
@Singleton
public class CountController extends Controller {

    private final Counter counter;
    private final String key = "count";
    Jedis jedis;
    Logger.ALogger log;

    @Inject
    public CountController(Counter counter) {

       try {

           jedis = new Jedis("localhost");

       } catch (Exception ex) {

           log.info("Exception Occured while connecting to Redis. Message:" + ex.getMessage() );
       }

       log = Logger.of(CountController.class);
       this.counter = counter;
    }

    /**
     * An action that responds with the {@link Counter}'s current
     * count. The result is plain text. This action is mapped to
     * <code>GET</code> requests with a path of <code>/count</code>
     * requests by an entry in the <code>routes</code> config file.
     */
    public Result count() {

        String count = null;

        if(jedis != null)
            count = jedis.get(key);

        log.info("Count found from db:" + count);

        if(count == null) {
            count = Integer.toString(1);
        } else {
            count = Integer.toString(Integer.parseInt(count) + 1);
        }

        if(jedis != null)
            jedis.set(key,count);

        return ok(count);
    }

}
