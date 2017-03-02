package IntegrationTests;

import logics.WordLogic;
import objects.DictionaryWord;
import objects.PartsOfSpeechSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.test.WithApplication;
import utilities.DictUtil;
import utilities.LogPrint;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by tahsinkabir on 1/7/17.
 */
public class WordControllerTests extends WithApplication {

    LogPrint log;
    WordLogic wordLogic;

    int NUMBER_OF_WORDS = 50;
    ArrayList<DictionaryWord> dictionary;

    @Before
    public void setup() {

        log = new LogPrint(WordControllerTests.class);
        wordLogic = WordLogic.factory();
        setupObjects();
    }

    private void setupObjects() {

        wordLogic.deleteAllWords();  //cleaning up for tests
        dictionary = new ArrayList<>( DictUtil.generateDictionaryWithRandomWords(NUMBER_OF_WORDS) );
        wordLogic.saveDictionaryWords(dictionary); //storing for tests
    }

    //WORD CRUD TESTS
    @Test
    public void storeWordTest() {

        DictionaryWord wordToBeStored = DictUtil.generateARandomWord(new PartsOfSpeechSet());
        String spelling = wordToBeStored.getWordSpelling();

        wordLogic.saveDictionaryWord(wordToBeStored);
        DictionaryWord retrievedWord = wordLogic.getDictionaryWordBySpelling(spelling);

        Assert.assertEquals(wordToBeStored.toJsonString(), retrievedWord.toJsonString());
    }

    @Test
    public void searchWordsByPrefix() throws Exception {

        String spelling = dictionary.get(0).getWordSpelling();
        String prefix = spelling.substring(0,1);

        Set<String> spellingsWithPrefixes = dictionary.stream()
                .filter( word -> word.getWordSpelling().startsWith(prefix) )
                .map( word-> word.getWordSpelling() )
                .collect( Collectors.toSet() );

        Set<String> results = wordLogic.searchWordsBySpelling(prefix, 10);
        Assert.assertEquals( spellingsWithPrefixes, results);
    }

    @Test
    public void getWordBySpelling() {

        String spelling = dictionary.get(0).getWordSpelling();
        DictionaryWord word = wordLogic.getDictionaryWordBySpelling(spelling);

        Assert.assertNotNull(word);
        Assert.assertEquals(spelling, word.getWordSpelling());
    }

    @Test
    public void totalWords() {
        long totalWords = wordLogic.totalWordCount();
        Assert.assertEquals(NUMBER_OF_WORDS, totalWords);
    }

}
