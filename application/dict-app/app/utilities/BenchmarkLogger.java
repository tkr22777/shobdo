package utilities;

import sun.rmi.runtime.Log;

/**
 * Created by tahsinkabir on 1/7/17.
 */
public class BenchmarkLogger {

    LogPrint log;
    Long startTime;

    public BenchmarkLogger(Class<?> class_type) {

        log = new LogPrint(class_type);
    }

    public BenchmarkLogger(LogPrint log) {

        this.log = log;
    }

    public void start() {

        startTime = System.currentTimeMillis();
    }

    public void end(String logString) {

        if(startTime == null)
            throw new IllegalStateException("Calling end without calling start previously!");

        if(log != null)
            log.info("Time Taken: " + ( System.currentTimeMillis() - startTime )  +"ms]" + logString);
        else
            System.out.println("[INFO][Time Taken: " + ( System.currentTimeMillis() - startTime )  +"ms]" + logString);

        startTime = null;
    }
}
