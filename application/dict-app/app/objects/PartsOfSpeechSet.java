package objects;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tahsinkabir on 8/21/16.
 */
//This should be store in a document to keep track of parts of speeches?
//This object/class could be used to nomalize if there are mistakes in the parts of speech entry
public class PartsOfSpeechSet {

    private Set<String> partsOfSpeeches;


    public Set<String> getPartsOfSpeeches() {
        return partsOfSpeeches;
    }

    public void setPartsOfSpeeches(Set<String> partsOfSpeeches) {
        this.partsOfSpeeches = partsOfSpeeches;
    }

    public void addPartsOfSpeech(String pos){

        if(partsOfSpeeches == null)
            partsOfSpeeches = new HashSet<>();

        partsOfSpeeches.add(pos);

    }

    public boolean isExistingPartsOfSpeech(String pos){

        if(partsOfSpeeches == null)
            return false;

        return partsOfSpeeches.contains(pos);

    }

    @Override
    public String toString() {
        return "PartsOfSpeechSet{" +
                "partsOfSpeeches=" + partsOfSpeeches +
                '}';
    }
}
