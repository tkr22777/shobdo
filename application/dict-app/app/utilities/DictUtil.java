package utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;

import java.util.Random;

/**
 * Created by tahsinkabir on 8/27/16.
 */
public class DictUtil {

    private static LogPrint log = new LogPrint(DictUtil.class);

    public static int randomInRange(int lowest, int highest){
        return new Random().nextInt( highest - lowest + 1) + lowest;
    }

    public static Object getObjectFromDocument(Document doc, Class<?> class_type) {

        doc.remove("_id");

        ObjectMapper mapper = new ObjectMapper();

        Object object = null;

        try {

            object = mapper.readValue( doc.toJson(), class_type );

        } catch ( Exception ex ){

            log.info( "Failed to map json word to dictionary word object. Ex: " + ex.getMessage() );
        }

        return object;
    }
}
