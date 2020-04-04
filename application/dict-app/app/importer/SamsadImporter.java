package importer;

import objects.Word;
import utilities.FileReadUtil;
import utilities.ShobdoLogger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Tahsin Kabir on 9/1/16.
 */

public class SamsadImporter {

    private ShobdoLogger log = new ShobdoLogger(SamsadImporter.class);

    private final String BANGLA_TO_BANGLA_FILE_LOCATION = "/Users/tahsin/Dropbox/Work/shobdo/resources/DictWebUChicagoSamsad_BANGLA_TO_BANGLA.txt";

    private static int printIndex = 0;

    private List<String> getLinesFromFile(String location, int N) {
        List<String> lines = new LinkedList<>();
        FileReadUtil fileReadUtilB2B = new FileReadUtil(location);
        for (int i = 0; i < N; i++) {
            String line = fileReadUtilB2B.getLine();
            if (line == null) {
                break;
            }
            lines.add(line);
        }
        fileReadUtilB2B.closeReader();
        return lines;
    }

    private Word createCrudeWord(String line) {

        int endIndexOfSpelling = line.indexOf("[");
        String spelling = line.substring(0, endIndexOfSpelling).trim();

        int endIndexOfEngSpell = line.indexOf("]");  //Eng pronunciation
        /* String endSpell = line.substring(endIndexOfSpelling + 1, endIndexOfEngSpell).trim(); */

        //Meaning
        String meaning = line.substring(endIndexOfEngSpell + 1).trim();

        return Word.builder()
            .spelling(spelling)
            .tempMeaningString(meaning)
            .build();
    }

    public List<Word> getDictiionary() {

        List<String> lines = getLinesFromFile(BANGLA_TO_BANGLA_FILE_LOCATION, 24000);

        return lines.stream()
            .map(line -> createCrudeWord(line))
            .filter(wd -> !(
                wd.getSpelling().contains("sup")
                    || wd.getSpelling().contains("style"))
                || wd.getSpelling().contains("719"))
            .collect(Collectors.toList());
    }

    public void test(List<Word> wordFromFiles) {

        Set<String> spellings = wordFromFiles.stream()
            .map(w -> w.getSpelling())
            .collect(Collectors.toSet());

        TreeMap<Character, Set<String>> charToSpelling = new TreeMap<>();
        TreeMap<Character, Set<String>> firstCharToSpelling = new TreeMap<>();
        spellings.forEach(
            spelling  -> {
                for (Character aChar: spelling.toCharArray()) {
                    charToSpelling.computeIfAbsent(aChar, v -> new HashSet<String>())
                        .add(spelling);
                }

                char firstChar = spelling.charAt(0);
                firstCharToSpelling.computeIfAbsent(firstChar, v -> new HashSet<>())
                    .add(spelling);
            }
        );

        System.out.println("Total Spellings:" + wordFromFiles.size());
        /*
        System.out.println("Total chars:" + charToSpelling.size());
        for (Character c: charToSpelling.keySet()) {
            System.out.print("Char " + c + " spelling count:" + charToSpelling.get(c).size() );
            System.out.println(" Ex Spelling:" + charToSpelling.get(c).stream().sorted().limit(100).collect(Collectors.toList()).toString() );
        }
        */

        System.out.println("Total first chars:" + firstCharToSpelling.size());
        System.out.println("First chars:" + firstCharToSpelling.keySet().toString());

        /*
        for (Character c: firstCharToSpelling.keySet()) {
            String outString = String.format("First Char:%c\t Char Value:%d \t Spelling Count:%d\t", c, (int)c, firstCharToSpelling.get(c).size()) ;
            System.out.print(outString);
            System.out.println(" Spelling:" + firstCharToSpelling.get(c).stream().sorted().limit(100).collect(Collectors.toList()).toString());
        }
        */

        /*
        wordFromFiles.stream().limit(200)
            .forEach(w -> System.out.println(w.toAPIJsonNode().toString()));
         */

        /*
        Collections.sort(lines, Comparator.comparingInt(String::length));
        System.out.println("Lines before:" + lines.size());
        List<String> fileredlines = lines.stream()
            .filter(
                m -> (
                    ! ( m.contains("b")
                    ||  m.contains("☐")
                    ||  m.contains("<eng")
                    ||  m.contains("<sup")
                    ||  m.startsWith("(")
                    )
                )
            ).collect(Collectors.toList());
        System.out.println("Filtered lines:" + fileredlines.size());
        //fileredlines.forEach(line -> System.out.println(line));

        lines.removeAll(fileredlines);
        System.out.println("Remaining Lines:" + lines.size());
        lines.stream().limit(1000).forEach(line -> System.out.println(line));
        return null;
        */
    }
}
