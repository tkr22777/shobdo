package utilities;

import objects.DictionaryWord;
import objects.Meaning;
import objects.MeaningForPartsOfSpeech;

import java.util.*;

/**
 * Created by tahsinkabir on 9/1/16.
 */

//This is a helper class to export Samsad database file's entries to dictionary objects

public class SamsadExporter {

    private LogPrint log = new LogPrint(SamsadExporter.class);

    public static final ArrayList<String> blueBucket = new ArrayList<>();
    public static final ArrayList<String> glassBucket = new ArrayList<>();
    public static final ArrayList<String> supBucket = new ArrayList<>();
    public static final ArrayList<String> commaBucket = new ArrayList<>();
    public static final ArrayList<String> dashBucket = new ArrayList<>();
    public static final ArrayList<String> spaceBucket = new ArrayList<>();

    public static final ArrayList<String> simpleBucket = new ArrayList<>();
    public static final ArrayList<String> simpleMeaningBucket = new ArrayList<>();
    public static final ArrayList<String> understandableSimpleMeaningBucket = new ArrayList<>();

    public static final Set<String> typeMeaningSet = new HashSet<>();

    public final String BANGLA_TO_BANGLA_FILE_LOCATION =
            "../../../../data/DictWebUChicagoSamsad_BANGLA_TO_BANGLA.txt";

    public static final Map<String,String> MAP_OF_TYPES = new HashMap<>();
    public static Set<String> SET_OF_TYPES = new HashSet<>();

    public void setup(){

        MAP_OF_TYPES.put("বি","বিশেষ্য");
        MAP_OF_TYPES.put("বিন","বিশেষণ");
        MAP_OF_TYPES.put("সর্ব","সর্বনাম");
        MAP_OF_TYPES.put("অব্য","অব্যয়");
        MAP_OF_TYPES.put("ক্রি","ক্রিয়া");

        SET_OF_TYPES = new HashSet<>( MAP_OF_TYPES.values() );

    }

    public List<DictionaryWord> getDictiionary() {

        setup();

        ReadFile readFileB2B = new ReadFile(BANGLA_TO_BANGLA_FILE_LOCATION);

        String line = "";

        int lines_to_read = 240000;

        List<DictionaryWord> words = new ArrayList<DictionaryWord>(lines_to_read);

        int skip = 0;
        for ( int i = 0; i < lines_to_read ; i++ ) {

            line = readFileB2B.getLine();

            if (line == null)
                break;

            if(i < skip)
                continue;

            DictionaryWord word = createWord(line);

            if(word != null)
                words.add( word );
        }

        labTest();

        return words;
    }


    public DictionaryWord createWord(String line) {

        DictionaryWord word = new DictionaryWord();

        word.setExtraMetaValue("Main String", line, false);

        List<String> list = new ArrayList<>( Arrays.asList( line.split("\\[") ) );

        //Spelling
        String spelling = list.get(0).trim();
        word.setWordSpelling( spelling );

        String meaning = list.get(1).trim();
        int  index = meaning.indexOf(']');
        String engPronunciation = meaning.substring(0,index);
        word.setExtraMetaValue( "Eng Pronunciation", engPronunciation, false);

        word.setExtraMetaValue( "EXAMPLE SET", "NO", false);

        meaning = meaning.substring(index+1).trim();
        word.setExtraMetaValue("Meaning", meaning, false);

        boolean simpleSpelling = true;

        if(spelling.contains("sup")) {
            supBucket.add(spelling);
            simpleSpelling = false;
            word.setExtraMetaValue("Spelling Contains 'sup'", "YES", false);
        }

        if (spelling.contains(",")) {
            commaBucket.add(spelling);
            simpleSpelling = false;
            word.setExtraMetaValue("Spelling Contains ','", "YES", false);
        }

        if (spelling.contains("-")) {
            dashBucket.add(spelling);
            simpleSpelling = false;
            word.setExtraMetaValue("Spelling Contains '-'", "YES", false);
        }

        if (spelling.contains(" ")) {
            spaceBucket.add(spelling);
            simpleSpelling = false;
            word.setExtraMetaValue("Spelling Contains ' '", "YES", false);
        }

        if( simpleSpelling ) {
            simpleBucket.add(spelling);
            word.setExtraMetaValue("SIMPLE SPELLING", "YES", false);
        }

        boolean simpleMeaning = true;

        if(meaning.contains("<b>")) {
            simpleMeaning = false;
            word.setExtraMetaValue("Meaning Contains ' '", "<b>", false);
        }

        if(meaning.contains("☐")) {
            simpleMeaning = false;
            word.setExtraMetaValue("Meaning Contains ' '", "☐", false);
        }

        if(meaning.contains("<eng")) {
            simpleMeaning = false;
            word.setExtraMetaValue("Meaning Contains ' '", "<eng", false);
        }

        if(meaning.contains("sup")) {
            simpleMeaning = false;
            word.setExtraMetaValue("Meaning Contains ' '", "sup", false);
        }

        if( meaning.substring(0,1).equalsIgnoreCase("(")) {
            simpleMeaning = false;
            word.setExtraMetaValue("Starts with", "(", false);
        }

        if( simpleMeaning ) {

            simpleMeaningBucket.add(meaning);

            word.setExtraMetaValue("SIMPLE MEANING", "YES", false);

            int indexOfDot = meaning.indexOf(".");

            String typePrefix = null;
            if(indexOfDot > 0 && indexOfDot < 5) {

                typePrefix = meaning.substring(0, indexOfDot).trim();

                if(MAP_OF_TYPES.get(typePrefix) != null) {

                    understandableSimpleMeaningBucket.add( meaning );

                    typeMeaningSet.add(typePrefix);

                    word.setExtraMetaValue("UNDERSTANDABLE TYPE", "YES", false);

                    String meaningId = "MN_" + UUID.randomUUID();

                    String type = MAP_OF_TYPES.get(typePrefix);

                    String meaningAfterType = meaning.substring(indexOfDot + 1).trim();

                    String example = "__EXAMPLE_NOT_SET__";

                    int strength = -1;

                    Meaning meaningObj = new Meaning(meaningId, type, meaningAfterType, example, -1 );

                    MeaningForPartsOfSpeech meaningForPartsOfSpeech = new MeaningForPartsOfSpeech(type,
                           Arrays.asList(meaningObj) );

                    word.addMeaningForPartsOfSpeech(meaningForPartsOfSpeech);

                }
            }

        }

        return word;

    }

    public void labTest(){

        boolean dontPrint = true;

        //dontPrint = false ;

        if(dontPrint)
            return;

        /*
        for(String supString: supBucket)
            log.info( "Sup String: '" + supString + "'" );
        for(String commaString: commaBucket)
            log.info( "Comma String: '" + commaString  + "'");


        for(String simpleSpelling: simpleBucket)
            log.info( "Spelling String: '" + simpleSpelling + "'");
        */

        for(String meaning: simpleMeaningBucket) {
            log.info("Meaning String: '" + meaning + "'");
        }

        log.info( "Total Type: '" + typeMeaningSet.toString() + "'");

        for(String type: typeMeaningSet) {
            log.info("Type String: '" + type + "'");
        }
        for(String type: MAP_OF_TYPES.keySet()) {
            log.info("Type In Bank String: '" + type + "'");
        }

        log.info( "Total Rest: '" + simpleBucket.size() + "'");

    }

}
