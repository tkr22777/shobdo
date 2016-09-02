package utilities;

import objects.DictionaryWord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tahsinkabir on 9/1/16.
 */

//This is a helper class to export Samsad database file's entries to dictionary objects

public class SamsadExporter {

    private LogPrint log = new LogPrint(SamsadExporter.class);

    public final String BANGLA_TO_BANGLA_FILE_LOCATION =
            "../../../../data/DictWebUChicagoSamsad_BANGLA_TO_BANGLA.txt";

    public DictionaryWord createWord(String line) {

        log.info(line);

        DictionaryWord word = new DictionaryWord();

        word.setWordSpelling(line);

        return word;

    }

    public void main() {

        ReadFile readFileB2B = new ReadFile(BANGLA_TO_BANGLA_FILE_LOCATION);

        String line = "";

        int lines_to_read = 10;

        List<DictionaryWord> words = new ArrayList<DictionaryWord>(lines_to_read);

        for ( int i = 0; i < lines_to_read && line != null ; i++ ) {

            line = readFileB2B.getLine();

            words.add( createWord(line) );
        }
    }
}
