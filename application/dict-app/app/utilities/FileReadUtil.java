package utilities;

import java.io.BufferedReader;
import java.io.FileReader;

public class FileReadUtil {

    private ShobdoLogger log = new ShobdoLogger(FileReadUtil.class);
    private BufferedReader reader;

    public FileReadUtil(String fileLocation) {
        reader = getReader(fileLocation);
    }

    private BufferedReader getReader(String fileLocation) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileLocation));
        } catch (Exception ex){
            log.info("Error reading opening file.");
            ex.printStackTrace();
        }
        return reader;
    }

    public String getLine() {
        try {
            return reader.readLine();
        } catch (Exception ex) {
            return "ExcEPTIoNal";
        }
    }

    public void closeReader() {
        try {
            reader.close();
        } catch (Exception ex){
            log.info("Error closing file.");
            ex.printStackTrace();
        }
    }
}
