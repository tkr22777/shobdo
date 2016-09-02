package utilities;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by tahsinkabir on 9/1/16.
 */
public class ReadFile {

    private LogPrint log = new LogPrint(ReadFile.class);

    BufferedReader reader;

    public ReadFile(String fileLocation) {

        reader = getReader(fileLocation);

    }

    public String getLine() {

        String line = null;

        if(reader == null)
            return line;

        try{

            line = reader.readLine();

        } catch (Exception ex) {

            log.info("Error reading line file. Exception:" + ex.getStackTrace().toString());
        }

        return line;
    }

    public void closeReader() {

        try {

            reader.close();

        } catch (Exception ex){

            log.info("Error closin file. Exception:" + ex.getStackTrace().toString());
        }
    }

    private BufferedReader getReader(String fileLocation) {

        BufferedReader reader = null;

        try{

            reader = new BufferedReader( new FileReader(fileLocation));

        } catch (Exception ex){

            log.info("Error reading opening file. Exception:" + ex.getStackTrace().toString());

        }

        return reader;
    }

}
