package utilities;


/**
 * Created by tahsinkabir on 8/21/16.
 */
public class LogPrint {

    Class<?> class_type;

    public LogPrint(Class<?> class_type){

        this.class_type = class_type;

    }

    public void info(String log){

        System.out.println("[" + class_type.toString() + "]:" + log );

    }

}
