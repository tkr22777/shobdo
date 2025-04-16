package utilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class ShobdoLogger {

    private final Class<?> class_type;

    public ShobdoLogger(Class<?> class_type){
        this.class_type = class_type;
    }

    public void info(String log){
        System.out.println("[INFO]["+ getCurrentTimeStamp() +"]"+"[" + class_type.toString() + "]:" + log );
    }

    public void debug(String log){
        System.out.println("[DEBUG]["+ getCurrentTimeStamp() +"]"+"[" + class_type.toString() + "]:" + log );
    }

    public void error(String log){
        System.out.println("[ERROR]["+ getCurrentTimeStamp() +"]"+"[" + class_type.toString() + "]:" + log );
    }

    public void error(String log, Throwable throwable){
        System.out.println("[ERROR]["+ getCurrentTimeStamp() +"]"+"[" + class_type.toString() + "]:" + log + "\n" + getStackTraceAsString(throwable));
    }

    private String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dateFormat.format(new Date());
    }
    
    private String getStackTraceAsString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}
