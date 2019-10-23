package utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tahsin Kabir on 8/21/16.
 */
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

    private String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dateFormat.format(new Date());
    }
}
