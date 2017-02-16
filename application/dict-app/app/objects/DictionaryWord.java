package objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import utilities.Constants;
import utilities.JsonUtil;
import utilities.LogPrint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by tahsinkabir on 8/21/16.
 */
public class DictionaryWord extends BaseWord {

    //arrangementType is the way the meanings of this word are arranged now
    //e.g. by parts of speech or by strength of the meaning (are not arranged by parts of speech, such as V, N , N , P, V)
    //The following array list can be represented as (arranged by meaning strength):
    //{ { V { A1, A2 } } , { N { A3 } }, { N { A4, A5} } , { V { A6 } } }
    //or as (arranged by combined strength for each of the parts of speeches and words ordered within them):
    //{ { V { A1, A2, A6 } } , { N { A3, A4, A5 } } } //<-- lets only support this

    int version = 0;
    boolean reviewed = false;
    boolean diplayToPublic = false;
    String arrangementType = null; //Arrangement partsOfSpeech will be used in V2
    ArrayList<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public boolean isDiplayToPublic() {
        return diplayToPublic;
    }

    public void setDiplayToPublic(boolean diplayToPublic) {
        this.diplayToPublic = diplayToPublic;
    }

    public String getArrangementType() {
        return arrangementType;
    }

    public void setArrangementType(String arrangementType) {
        this.arrangementType = arrangementType;
    }

    public DictionaryWord() {
        super();

    }

    public DictionaryWord(String wordId,
                          String wordSpelling)
    {
        super(wordId, wordSpelling);
    }

    public DictionaryWord(String wordId,
                          String wordSpelling,
                          String arrangementType,
                          ArrayList<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches)
    {

        super(wordId, wordSpelling);
        this.arrangementType = arrangementType;
        this.meaningForPartsOfSpeeches = meaningForPartsOfSpeeches;
    }

    public DictionaryWord(String wordId,
                          String wordSpelling,
                          int timesSearched,
                          String linkToPronunciation,
                          String arrangementType,
                          Collection<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches,
                          Map<String,List<String>> extraMeta)
    {
        super(wordId, wordSpelling, timesSearched, linkToPronunciation, extraMeta);
        this.arrangementType = arrangementType;
        this.meaningForPartsOfSpeeches = new ArrayList<>(meaningForPartsOfSpeeches);
    }

    public DictionaryWord(String wordInJsonString){

        super();

        ObjectMapper mapper = new ObjectMapper();

        DictionaryWord word = null;

        try {

           word = mapper.readValue(wordInJsonString, DictionaryWord.class);

        } catch (Exception ex){

            LogPrint log = new LogPrint(DictionaryWord.class);
            log.info("Error converting jsonString to Object. Exception:" + ex.getStackTrace().toString());
        }

    }

    public ArrayList<MeaningForPartsOfSpeech> getMeaningForPartsOfSpeeches() {
        return meaningForPartsOfSpeeches;
    }

    public void setMeaningForPartsOfSpeeches(ArrayList<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches) {
        this.meaningForPartsOfSpeeches = meaningForPartsOfSpeeches;
    }

    public void addMeaningForPartsOfSpeech(MeaningForPartsOfSpeech aMeaningForPartsOfSpeech) {

        if(meaningForPartsOfSpeeches == null)
            meaningForPartsOfSpeeches = new ArrayList<>();

        meaningForPartsOfSpeeches.add(aMeaningForPartsOfSpeech);
    }

    public String toJsonString() {

        return JsonUtil.toJsonString(this);
    }

    @Override
    public String toString() {

        if(Constants.CUSTOM_STRING)
           return customToStringDictionaryWord();
        else
            return toJsonString();
    }

    public String customToStringDictionaryWord(){
        return "\n\n\tDictionary Word { " +
                //"\n\n\t\t Arrangement Type = '" + arrangementType + '\'' +
                "\n\n\t\t Meaning For Parts Of Speeches = " + meaningForPartsOfSpeeches +
                "\n\n\t\t " + super.toString() +
                "\n\n\t}";
    }

}
