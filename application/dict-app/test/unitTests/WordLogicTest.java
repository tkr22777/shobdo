package unitTests;

import cache.WordCache;
import daos.WordDao;
import logics.WordLogic;
import objects.DictionaryWord;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import utilities.LogPrint;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.*;
/**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
public class WordLogicTest {

    public LogPrint log = new LogPrint(WordLogicTest.class);

    private WordDao mockWordDao;
    private WordCache mockWordCache;

    private WordLogic wordLogic;

    private String wordSpelling = "পিটন";
    private DictionaryWord theWord;

    @Before
    public void setup() {

        setupMocks();

        setupObjects();
    }

    public void setupMocks() {

        mockWordDao = mock(WordDao.class);

        mockWordCache = mock(WordCache.class);

        wordLogic = new WordLogic(mockWordDao, mockWordCache);

    }

    public void setupObjects() {

        theWord = new DictionaryWord();
        theWord.setWordSpelling(wordSpelling);


    }

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDictionaryWordBySpelling_spellingIsNull_throwsException() {

        wordLogic.getDictionaryWordBySpelling(null);
    }

    @Test
    public void getDictionaryWordBySpelling_foundCached_doNotCallDB() {

        when(mockWordCache.getDictionaryWordBySpellingFromCache(wordSpelling)).thenReturn(theWord);

        wordLogic.getDictionaryWordBySpelling(wordSpelling);

        verify(mockWordDao, never()).getDictionaryWordBySpelling(anyString());

        verify(mockWordCache, never()).cacheDictionaryWord(any(DictionaryWord.class));
    }

    @Test
    public void getDictionaryWordBySpelling_notCached_callDBandCache() {

        when(mockWordCache.getDictionaryWordBySpellingFromCache(wordSpelling)).thenReturn(null);
        when(mockWordDao.getDictionaryWordBySpelling(wordSpelling)).thenReturn(theWord);

        wordLogic.getDictionaryWordBySpelling(wordSpelling);

        verify(mockWordDao, times(1) ).getDictionaryWordBySpelling(wordSpelling);

        verify(mockWordCache, times(1) ).cacheDictionaryWord(theWord);
    }

}
