package cache;

public class WordCache{
    
    private static boolean USE_REDIS = true;
    private static final String DEFAULT_REDIS_HOSTNAME="localhost";
    
    private Jedis jedis;

    /* Redis key words */
    private final String REDIS_SERACH_WORD_BY_SPELLING = "SRC_WD_BY_SPL";
    private final String REDIS_GET_WORD_BY_SPELLING = "GET_WD_BY_SPL";
    
    /*Redis expire time*/
    private boolean USE_REDIS_EXPIRATION_TIME = false;
    private final int REDIS_EXPIRE_TIME = 10; //in seconds
    
    private LogPrint log = new LogPrint(WordCache.class);
    
    public WordCache() {
            
        jedis = new Jedis( getHostname() );
         
    }
    
    public String getHostname() {
        //You may return from environment from here
        return DEFAULT_REDIS_HOSTNAME;
    }
    
    public Jedis getJedis( String hostname) {

        Jedis jedis = null;

        try {

            jedis = new Jedis(hostname);

        } catch (Exception ex) {

            log.info("Exception Occured while connecting to Redis. Message:" + ex.getMessage() );
        }
        
        return jedis;
    }
     

    
}
