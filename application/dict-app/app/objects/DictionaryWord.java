package objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by tahsinkabir on 8/21/16.
 */
public class DictionaryWord extends BaseWord {

    //arrangementType is the way the meanings of this word is arranged now
    //e.g. by POS or by strength of the meaning where not arranged by POSs V, N , N , P, V
    //The following array list can be represented as (arrange by meaning strength):
    //{ { V { A1, A2 } } , { N { A3 } }, { N { A4, A5} } , { V { A6 } } }
    //or as (arranged by combined strength for each of the parts of speech and words worders within them):
    //{ { V { A1, A2, A6 } } , { N { A3, A4, A5 } } } //<-- lets only support this

    String arrangementType = null; //There will be only one arrangement for start

    ArrayList<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches;

    public DictionaryWord(){
        super();
    }

    public DictionaryWord(String wordId, String wordSpelling) {
        super(wordId, wordSpelling);
    }

    public DictionaryWord(String wordId, String wordSpelling, String arrangementType, ArrayList<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches) {
        super(wordId, wordSpelling);
        this.arrangementType = arrangementType;
        this.meaningForPartsOfSpeeches = meaningForPartsOfSpeeches;
    }

    public DictionaryWord(String wordId, String wordSpelling, int timesSearched, String linkToPronunciation, String arrangementType, Collection<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches) {
        super(wordId, wordSpelling, timesSearched, linkToPronunciation);
        this.arrangementType = arrangementType;
        this.meaningForPartsOfSpeeches = new ArrayList<>(meaningForPartsOfSpeeches);
    }

    public String getArrangementType() {
        return arrangementType;
    }

    public void setArrangementType(String arrangementType) {
        this.arrangementType = arrangementType;
    }

    public ArrayList<MeaningForPartsOfSpeech> getMeaningForPartsOfSpeeches() {
        return meaningForPartsOfSpeeches;
    }

    public void setMeaningForPartsOfSpeeches(ArrayList<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches) {
        this.meaningForPartsOfSpeeches = meaningForPartsOfSpeeches;
    }

    @Override
    public String toString() {
        return "DictionaryWord{" +
                "arrangementType='" + arrangementType + '\'' +
                ", meaningForPartsOfSpeeches=" + meaningForPartsOfSpeeches.toString() +
                '}';
    }
}
