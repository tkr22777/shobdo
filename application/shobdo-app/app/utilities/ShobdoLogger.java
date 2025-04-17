package utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around SLF4J logger to maintain the same API.
 * SLF4J will automatically route to Logback, which is configured via logback.xml
 */
public final class ShobdoLogger {

    private final Logger logger;

    public ShobdoLogger(Class<?> class_type) {
        this.logger = LoggerFactory.getLogger(class_type);
    }

    public void info(String log) {
        logger.info(log);
    }

    public void debug(String log) {
        logger.debug(log);
    }

    public void error(String log) {
        logger.error(log);
    }

    public void error(String log, Throwable throwable) {
        logger.error(log, throwable);
    }
}
