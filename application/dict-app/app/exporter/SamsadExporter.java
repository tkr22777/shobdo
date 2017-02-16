package exporter;

import logics.WordLogic;
import objects.DictionaryWord;
import utilities.Constants;
import utilities.DictUtil;
import utilities.LogPrint;
import utilities.ReadFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by tahsinkabir on 9/1/16.
 */

//This is a helper class to export Samsad database file's entries to dictionary objects

public class SamsadExporter {

    private LogPrint log = new LogPrint(SamsadExporter.class);

    private static final ArrayList<String> supBucket = new ArrayList<>();
    private static final ArrayList<String> commaBucket = new ArrayList<>();
    private static final ArrayList<String> dashBucket = new ArrayList<>();
    private static final ArrayList<String> spaceBucket = new ArrayList<>();

    private static final ArrayList<String> simpleBucket = new ArrayList<>();
    private static final ArrayList<String> simpleMeaningBucket = new ArrayList<>();
    private static final ArrayList<String> understandableSimpleMeaningBucket = new ArrayList<>();

    private static final Set<String> typeMeaningSet = new HashSet<>();

    private final String BANGLA_TO_BANGLA_FILE_LOCATION = "../../../../data/DictWebUChicagoSamsad_BANGLA_TO_BANGLA.txt";

    private static final Map<String,String> MAP_OF_TYPES = new HashMap<>();
    private static Set<String> SET_OF_TYPES = new HashSet<>();

    private static int printIndex = 0;


    public void setup(){

        MAP_OF_TYPES.put("বি","বিশেষ্য");
        MAP_OF_TYPES.put("বিন","বিশেষণ");
        MAP_OF_TYPES.put("সর্ব","সর্বনাম");
        MAP_OF_TYPES.put("অব্য","অব্যয়");
        MAP_OF_TYPES.put("ক্রি","ক্রিয়া");

        SET_OF_TYPES = new HashSet<>( MAP_OF_TYPES.values() );
    }

    public Collection<DictionaryWord> getDictiionary() {

        setup();

        ReadFile readFileB2B = new ReadFile(BANGLA_TO_BANGLA_FILE_LOCATION);

        String line = "";

        int lines_to_read = 240000;

        ArrayList<DictionaryWord> words = new ArrayList<DictionaryWord>(lines_to_read);

        for ( int i = 0; i < lines_to_read ; i++ ) {

            line = readFileB2B.getLine();

            if (line == null)
                break;

            DictionaryWord word = createCrudeWord(line);

            if(word != null)
                words.add( word );
        }

        return fixSpellingAndMeanings(words);
    }


    public DictionaryWord createCrudeWord(String line) {

        DictionaryWord word = new DictionaryWord();

        word.setWordId( Constants.WORD_ID_PREFIX + UUID.randomUUID() );
        word.setExtraMetaValue(Constants.ORIGINAL_STRING, line);

        //Spelling
        int endIndexOfSpelling = line.indexOf("[");
        String spelling = line.substring(0, endIndexOfSpelling).trim();
        word.setWordSpelling(spelling);

        //Eng pronunciation
        int endIndexOfEngPronunciation = line.indexOf("]");
        String engPronunciation = line.substring(endIndexOfSpelling + 1, endIndexOfEngPronunciation).trim();
        word.setExtraMetaValue( Constants.ENG_PRONUN_STRING, engPronunciation);

        //Meaning
        String meaning = line.substring(endIndexOfEngPronunciation + 1, line.length()).trim();
        word.setExtraMetaValue( Constants.MEANING_STRING, meaning);

        return word;
    }

    public Collection<DictionaryWord> fixSpellingAndMeanings(ArrayList<DictionaryWord> words) {

        //What should we do about these unfixed ones?!
        FixSpellingReturn fixSpellingReturn =  fixSpelling(words);
        FixMeaningReturn fixMeaningReturn = fixMeaning(fixSpellingReturn.fixed);

        return fixSpellingReturn.fixed;
    }

    class FixMeaningReturn {

        Collection<DictionaryWord> fixed;
        Collection<DictionaryWord> unfixed;
    }

    private Map<String, DictionaryWord> findSpellingsWithSimpleMeanings(Collection<DictionaryWord> words) {

        Map<String, DictionaryWord> simpleMeaningWords = new HashMap<>();

        List<String> allMeaning = new ArrayList<>();

        for(DictionaryWord word: words) {

            List<String> meanings = word.retrieveExtraMetaValueForKey(Constants.MEANING_STRING);

            allMeaning.addAll(meanings);

            List<String> filtered =  meanings.stream().filter(
                    m -> (
                            //Doesn't contain
                            ! (      m.contains("b")
                                    ||  m.contains("☐")
                                    ||  m.contains("<eng")
                                    ||  m.contains("<sup")
                                    ||  m.startsWith("(")
                            )
                    ) ).collect(Collectors.toList());

            if(filtered.size() == meanings.size() ) {

                //DictUtil.printStringsByTag( word.getWordSpelling() + " Meaning(s): ", meanings, 0, 100, false);

                simpleMeaningWords.put(word.getWordSpelling(), word);
            }
        }

        return simpleMeaningWords;
    }

    private LinkedHashMap<String, Set<String>> getAllMeanings(Collection<DictionaryWord> words){

        LinkedHashMap<String,Set<String>> allMeaning = new LinkedHashMap<>();

        for(DictionaryWord word: words) {

            for(String meaning: word.retrieveExtraMetaValueForKey(Constants.MEANING_STRING)){

                Set<String> spellings = allMeaning.get(meaning);

                if(spellings == null)
                    spellings = new HashSet<>();

                spellings.add(word.getWordSpelling());

                allMeaning.put(meaning, spellings);
            }
        }

        return allMeaning;
    }


    public FixMeaningReturn fixMeaning(Collection<DictionaryWord> words) {

        FixMeaningReturn fixMeaningReturn = new FixMeaningReturn();

        LinkedHashMap<String,Set<String>> meanings = getAllMeanings(words);

        meanings.entrySet().stream()
                .sorted(Map.Entry.comparingByKey() )
                .filter( entry -> entry.getValue().size() < 5 && entry.getValue().size() > 2)
                .forEach(entry -> log.info(entry.getValue() + " : " + entry.getKey() ) );

        //DictUtil.printStringsByTag("All Meangins:", meanings, 1000 , 1000, false);

        Map<String, DictionaryWord> wordMap = createWordMapFromListDiplicateSpellingFix(words);
        Map<String, DictionaryWord> simpleMeaningWordMap = findSpellingsWithSimpleMeanings(words);
        Map<String, DictionaryWord> wordMapFirstGen = DictUtil.removeKeyValuesForKeys(wordMap, simpleMeaningWordMap.keySet());


        /*
        boolean simpleMeaning = true;

        if(meaning.contains("<b>")) {
            simpleMeaning = false;
            word.setExtraMetaValue("MeaningContainsB", "<b>", false);
        }

        if(meaning.contains("☐")) {
            simpleMeaning = false;
            word.setExtraMetaValue("MeaningContainsBox", "☐", false);
        }

        if(meaning.contains("<eng")) {
            simpleMeaning = false;
            word.setExtraMetaValue("MeaningContainsEng", "<eng", false);
        }

        if(meaning.contains("sup")) {
            simpleMeaning = false;
            word.setExtraMetaValue("MeaningContainsSup", "sup", false);
        }

        if( meaning.substring(0,1).equalsIgnoreCase("(")) {
            simpleMeaning = false;
            word.setExtraMetaValue("StartsWithBracket", "(", false);
        }

        if( simpleMeaning ) {

            simpleMeaningBucket.add(meaning);

            word.setExtraMetaValue("SIMPLE_MEANING", "YES", false);

            int indexOfDot = meaning.indexOf(".");

            String typePrefix = null;

            if(indexOfDot > 0 && indexOfDot < 5) {

                typePrefix = meaning.substring(0, indexOfDot).trim();

                if( MAP_OF_TYPES.get(typePrefix) != null) {

                    understandableSimpleMeaningBucket.add( meaning );

                    typeMeaningSet.add(typePrefix);

                    word.setExtraMetaValue("UNDERSTANDABLE_TYPE", "YES", false);

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
        */

        return fixMeaningReturn;
    }


    private Map<String,DictionaryWord> createWordMapFromListDiplicateSpellingFix(Collection<DictionaryWord> words) {

        //Has duplicate key
        Map<String,DictionaryWord> wordMap =  new HashMap<>();

        Set<String> duplicates = new HashSet<>();

        for(DictionaryWord word: words) {

            String spelling = word.getWordSpelling();

            if(wordMap.containsKey(spelling)) {
                // ^^ means duplicate found, we are adding them to the same words meta map without updating

                DictionaryWord firstOne = wordMap.get(spelling);
                firstOne.setExtraMetaValue(Constants.MEANING_STRING, word.getExtraMetaMap().get(Constants.MEANING_STRING));
                firstOne.setExtraMetaValue(Constants.ORIGINAL_STRING, word.getExtraMetaMap().get(Constants.ORIGINAL_STRING));
                firstOne.setExtraMetaValue(Constants.ENG_PRONUN_STRING, word.getExtraMetaMap().get(Constants.ENG_PRONUN_STRING));
                wordMap.put(spelling, firstOne);
                duplicates.add(spelling);

            } else {

                wordMap.put(word.getWordSpelling(), word);
            }
        }
        return wordMap;
    }

    //Mostly duplicate spelling fix
    private Map<String, DictionaryWord> fixSupSpellingWords(Map<String,DictionaryWord> allWordMap, List<String> supSpellings) {

        Map<String,DictionaryWord> filteredSupWordMap = DictUtil.filterForKeys(allWordMap, new HashSet<>(supSpellings));

        //DictUtil.printStringsByTag("Sup Words", new ArrayList<>( filteredSupWordMap.values()) , 0, 10, true);

        List<DictionaryWord> words = new ArrayList<>();

        List<String> allSpellingList = new ArrayList<>();
        int print = 0;

        for(DictionaryWord word: filteredSupWordMap.values()){

            String spelling = word.getWordSpelling();

            if( spelling.contains("<") ){

                if(print < 10)
                    log.info("Spelling before:" + spelling);

                spelling = spelling.substring(0, spelling.indexOf("<"));

                if(print < 10) {
                    log.info("Spelling after: " + spelling);
                    print++;
                }

            } else {

                log.info("What the hell spelling: " + spelling);
            }

            allSpellingList.add(spelling);
            word.setWordSpelling(spelling);
            words.add(word);
        }


        log.info("All spelling list size:"  + allSpellingList.size());
        HashSet<String> allspellingSet = new HashSet<>(allSpellingList);
        log.info("All spelling set size:"  + allspellingSet.size()); //Should be less as they are duplicate and the meanings are merged

        Map<String, DictionaryWord> finishedSupWord = createWordMapFromListDiplicateSpellingFix(words);

        //DictUtil.printStringsByTag("Sup Words After:", new ArrayList<>( finishedSupWord.values()) , 0, 100, true);
        return finishedSupWord;
    }

    //Comma spelling fix
    private Map<String, DictionaryWord> fixCommaSpellingWords(Map<String,DictionaryWord> allWordMap, List<String> commaSpellings) {

        Map<String, DictionaryWord> filteredCommaWordMap = DictUtil.filterForKeys(allWordMap, new HashSet<>(commaSpellings));
        //DictUtil.printStringsByTag("Comma Words", new ArrayList<>(filteredCommaWordMap.values()), 0, 10, true);
        List<DictionaryWord> words = new ArrayList<>();

        int print = 0;

        for (DictionaryWord word : filteredCommaWordMap.values()) {

            String wordSpelling = word.getWordSpelling();

            ArrayList<DictionaryWord> newWords = new ArrayList<>();

            if (wordSpelling.contains(",")) {

                String[] spellings = wordSpelling.split(",");
                if (print < 40)
                    log.info("Spelling before:" + wordSpelling);
                if (print < 40) {
                    log.info("spellings after: " + spellings.toString());
                    print++;
                }

                for(String spelling: spellings) {

                    DictionaryWord newWord = WordLogic.copyToNewDictWordObject(word);

                    newWord.setWordSpelling(spelling.trim());

                    newWords.add(newWord);
                }

                if(newWords.size() > 5) {
                    log.info("More than 8 variations:" + wordSpelling);
                    log.info("More than 8 variations word:" + word);
                }

            } else {

                log.info("What the hell spelling in comma filter!: " + wordSpelling);
            }

            words.addAll(newWords);
        }

        Map<String, DictionaryWord> finishedCommaWords = createWordMapFromListDiplicateSpellingFix(words);
        //DictUtil.printStringsByTag("Comma Words After:", new ArrayList<>(finishedCommaWords.values()), 0, 10, false);

        return finishedCommaWords;
    }

    //Simple dash spelling fix
    private Map<String, DictionaryWord> fixSimpelDashSpellingWords(Map<String,DictionaryWord> dashWordMap) {

        //DictUtil.printStringsByTag("Dash Words", new ArrayList<>(dashWordMap.values()), 0, 10, true);
        List<DictionaryWord> words = new ArrayList<>();

        int print = 0;

        for (DictionaryWord word : dashWordMap.values()) {

            String wordSpelling = word.getWordSpelling();

            if (wordSpelling.contains("-")) {

                String newSpelling = new String(wordSpelling);

                newSpelling = newSpelling.replaceAll("-", "");

                if (print < 40)
                    log.info("Spelling before:" + wordSpelling);
                if (print < 40) {
                    log.info("spellings after: " + newSpelling);
                    print++;
                }

                DictionaryWord newWord = WordLogic.copyToNewDictWordObject(word);
                newWord.setWordSpelling(newSpelling);

                words.add(word);
                words.add(newWord);

            } else {
                words.add(word);
            }
        }

        Map<String, DictionaryWord> finishedDashWords = createWordMapFromListDiplicateSpellingFix(words);
        //DictUtil.printStringsByTag("Dash Words After:", new ArrayList<>(finishedDashWords.values()), 0, 10, false);

        return finishedDashWords;
    }

    class FixSpellingReturn {

        Collection<DictionaryWord> fixed;
        Collection<DictionaryWord> unfixed;
    }

    private FixSpellingReturn fixSpelling(ArrayList<DictionaryWord> words) {

        //Has duplicate key
        Map<String,DictionaryWord> wordMapClean = new HashMap<>();
        Map<String,DictionaryWord> wordMapFirstGen = createWordMapFromListDiplicateSpellingFix(words);

        //Getting all the available spelling string, might contain multiple actual string
        List<String> allSpellings = new ArrayList<>(wordMapFirstGen.keySet());
        Collections.sort(allSpellings);
        log.info("Total all spellings: " + allSpellings.size());
        //DictUtil.printStringsByTag("All Spelling:", allSpellings, 400 , 10, false);

        /*START OF SIMPLE SPELLING*/
        List<String> complexSpellings = allSpellings.stream()
                .filter( s -> s.contains("<")
                        || s.contains( ">")
                        || s.contains( ")")
                        || s.contains( "(")
                        || s.contains( "?")
                        || s.contains( ",")
                        || s.contains( "-")
                        || s.contains( " ")
                        || s.contains( "sup") )
                .collect(Collectors.toList());

        List<String> simpletons = new ArrayList<>( allSpellings);
        simpletons.removeAll(complexSpellings); //Found 14000 simpleton spellings
        log.info("Simpleton size: " + simpletons.size());

        //DictUtil.printStringsByTag("Simpletons spelling:", simpletons, 0, 10, true);
        Map<String, DictionaryWord> simpleWords = DictUtil.filterForKeys(wordMapFirstGen, new HashSet<>(simpletons) );
        wordMapClean.putAll(simpleWords);
        /*END OF SIMPLE*/


        Map<String,DictionaryWord> wordMapSecondGenSimpleRemoved = DictUtil.removeKeyValuesForKeys(wordMapFirstGen, new HashSet<>(simpletons) );
        log.info("Word map after simple removal size: " + wordMapSecondGenSimpleRemoved.size());

        /* Start of SUP */
        //Example 1: ধার<sup>1</sup>
        //Example 2: ঝোলা<sup>2</sup>'
        List<String> simpleSups = complexSpellings.stream()
                .filter( s ->
                        (   //contains the following
                            s.contains( "sup")
                                    ||  s.contains("<")
                                    ||  s.contains( ">")
                                    ||  s.contains( "?") )
                            && !( //does not contain the the following
                                       s.contains( ",")
                                    || s.contains( ")")
                                    || s.contains( "(")
                                    || s.contains( "-")
                                    || s.contains( " ") )
                ).collect(Collectors.toList());

        log.info("Simple Sup Size: " + simpleSups.size());
        //DictUtil.printStringsByTag("Simple sup:", simpleSups, 0, 10, true);
        Map<String,DictionaryWord> supWords = fixSupSpellingWords(wordMapSecondGenSimpleRemoved, simpleSups);
        wordMapClean.putAll(supWords);
        log.info("Total after simple and sup: " + wordMapClean.size());
        /* End of SUP */

        Map<String,DictionaryWord> wordMapThirdGenSimpleAndSupRemoved = DictUtil.removeKeyValuesForKeys(wordMapSecondGenSimpleRemoved, new HashSet<>(simpleSups) );
        log.info("Word map after simple and sup removal size: " + wordMapThirdGenSimpleAndSupRemoved.size());
        complexSpellings.removeAll(simpleSups);

        List<String> simpleComma = complexSpellings.stream()
                .filter( s ->
                        ( //contains the following
                            s.contains( ",")
                            && s.contains( " ")

                        ) && !( //does not contain the the following
                                s.contains( "sup")
                                || s.contains( ">")
                                || s.contains("<")
                                || s.contains( ")")
                                || s.contains( "(")
                                || s.contains( "?")
                                || s.contains( "-") )
                ).collect(Collectors.toList());

        log.info("Simple Comma Size: " + simpleComma.size());
        //DictUtil.printStringsByTag("Simple Comma(,):", simpleComma, 0, 10, true);

        Map<String,DictionaryWord> commaWords = fixCommaSpellingWords(wordMapThirdGenSimpleAndSupRemoved, simpleComma);
        wordMapClean.putAll(commaWords);
        log.info("Wordmap clean size:" + wordMapClean.size());

        Map<String,DictionaryWord> wordMapFourthGenSimpleSupCommaRemoved = DictUtil.removeKeyValuesForKeys(wordMapThirdGenSimpleAndSupRemoved, new HashSet<>(simpleComma) );
        log.info("Word map after simple, sup & comma removal size: " + wordMapFourthGenSimpleSupCommaRemoved.size());

        complexSpellings.removeAll(simpleComma);

        List<String> simpleDash = complexSpellings.stream()
                .filter( s ->
                        //contains the following
                        s.contains( "-")
                                && !( //does not contain the the following
                                s.contains( "sup")
                                        || s.contains( ">")
                                        || s.contains("<")
                                        || s.contains( "?")
                                        || s.contains( ")")
                                        || s.contains( "(")
                                        || s.contains( ",")
                                        || s.contains( " ") )
                ).collect(Collectors.toList());

        log.info("Simple Dash Size: " + simpleDash.size());
        //DictUtil.printStringsByTag("Simple Dash(-):", simpleDash, 0, 10, true);

        Map<String, DictionaryWord> dashWordMap = DictUtil.filterForKeys(wordMapFourthGenSimpleSupCommaRemoved, new HashSet<>(simpleDash));
        Map<String,DictionaryWord> fixedDashWords = fixSimpelDashSpellingWords(dashWordMap);
        wordMapClean.putAll(fixedDashWords);
        log.info("Wordmap clean size after dash:" + wordMapClean.size());

        Map<String,DictionaryWord> wordMapFifthGenSimpleSupCommaDashRemoved = DictUtil.removeKeyValuesForKeys(wordMapFourthGenSimpleSupCommaRemoved, new HashSet<>(simpleDash) );
        log.info("Word map after simple, sup, comma and dash removal size: " + wordMapFifthGenSimpleSupCommaDashRemoved.size());

        complexSpellings.removeAll(simpleDash);

        List<String> simpleCommaDash = complexSpellings.stream()
                .filter( s ->
                        //contains the following
                        s.contains( "-")
                        && s.contains( ",")
                                && s.contains( " ")
                                && !( //does not contain the the following
                                    s.contains( "sup")
                                        || s.contains( ">")
                                        || s.contains("<")
                                        || s.contains( "?")
                                        || s.contains( ")")
                                        || s.contains( "(") )
                ).collect(Collectors.toList());

        log.info("Simple simpleCommaDash Size: " + simpleCommaDash.size());
        //DictUtil.printStringsByTag("Simple Comma Dash(, -):", simpleCommaDash, 0, 10, true);

        Map<String,DictionaryWord> commaRemovedDashedMap = fixCommaSpellingWords(wordMapFifthGenSimpleSupCommaDashRemoved, simpleCommaDash);
        Map<String,DictionaryWord> commaDashRemovedCommaDashedMap = fixSimpelDashSpellingWords(commaRemovedDashedMap);
        wordMapClean.putAll(commaDashRemovedCommaDashedMap);

        Map<String,DictionaryWord> wordMapSixthGenSimpleSupCommaDashDashCommaRemoved = DictUtil.removeKeyValuesForKeys(wordMapFifthGenSimpleSupCommaDashRemoved, new HashSet<>(simpleCommaDash) );
        //DictUtil.printStringsByTag("Comma Removed Dashed: ", new ArrayList<>(commaDashRemovedCommaDashedMap.values()) , 0, 10, true);

        complexSpellings.removeAll(simpleCommaDash);

        log.info("Wordmap clean size after comma dash comma:" + wordMapClean.size());
        log.info("Complex Remaining: " + complexSpellings.size());
        //DictUtil.printStringsByTag("Complex :", complexSpellings, 0, 10, true);

        FixSpellingReturn fixSpellingReturn = new FixSpellingReturn();
        fixSpellingReturn.fixed = wordMapClean.values();
        fixSpellingReturn.unfixed = wordMapSixthGenSimpleSupCommaDashDashCommaRemoved.values();

        return fixSpellingReturn;
    }
}
