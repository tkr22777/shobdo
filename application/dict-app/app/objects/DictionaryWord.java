package objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import utilities.LogPrint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by tahsinkabir on 8/21/16.
 */
public class DictionaryWord extends BaseWord {

    private LogPrint log = new LogPrint(DictionaryWord.class);

    //arrangementType is the way the meanings of this word are arranged now
    //e.g. by parts of speech or by strength of the meaning (are not arranged by parts of speech, such as V, N , N , P, V)
    //The following array list can be represented as (arranged by meaning strength):
    //{ { V { A1, A2 } } , { N { A3 } }, { N { A4, A5} } , { V { A6 } } }
    //or as (arranged by combined strength for each of the parts of speeches and words ordered within them):
    //{ { V { A1, A2, A6 } } , { N { A3, A4, A5 } } } //<-- lets only support this

    //Arrangement type will be used in V2
    String arrangementType = null;

    public String getArrangementType() {
        return arrangementType;
    }

    public void setArrangementType(String arrangementType) {
        this.arrangementType = arrangementType;
    }

    ArrayList<MeaningForPartsOfSpeech> meaningForPartsOfSpeeches;

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
                          Map<String,String> extraMeta)
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

        String jsonString = null;

        try {

            jsonString = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);

        } catch (JsonProcessingException exception) {

            log.info("DW001: Json Processing Exception Message: " + exception.getMessage());
        }

        return jsonString;
    }

    @Override
    public String toString() {

        boolean customString = true;

        if(customString)
           return customToString();
        else
            return toJsonString();
    }

    public String customToString(){
        return "\n\n\tDictionary Word { " +
                //"\n\n\t\t Arrangement Type = '" + arrangementType + '\'' +
                "\n\n\t\t Meaning For Parts Of Speeches = " + meaningForPartsOfSpeeches +
                "\n\n\t\t " + super.toString() +
                "\n\n\t}";
    }

}
