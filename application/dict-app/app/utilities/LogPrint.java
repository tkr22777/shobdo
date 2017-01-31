package utilities;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by tahsinkabir on 8/21/16.
 */
public class LogPrint {

    Class<?> class_type;

    public LogPrint(Class<?> class_type){

        this.class_type = class_type;
    }

    public void info(String log){

        System.out.println("[INFO]["+ getCurrentTimeStamp() +"]"+"[" + class_type.toString() + "]:" + log );
    }

    public void debug(String log){

        System.out.println("[DEBUG]["+ getCurrentTimeStamp() +"]"+"[" + class_type.toString() + "]:" + log );
    }

    public String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return dateFormat.format(new Date());
    }

}
