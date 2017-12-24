package unitTests;

import cache.WordCache;
import daos.WordDao;
import logics.WordLogic;
import objects.Word;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import utilities.LogPrint;

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
    private Word theWord;

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

        theWord = new Word();
        theWord.setWordSpelling(wordSpelling);
    }

    //Create word
    @Test
    public void createWord_wordIdIsNotSet_createWordDaoCalled() {
        when(mockWordDao.createWord(any())).thenReturn(theWord);
        wordLogic.createWord(theWord);
        verify(mockWordDao, times(1)).createWord(any(Word.class));
        verify(mockWordCache, times(1)).cacheWord(any(Word.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWord_wordIdIsSet_throwsIAE() {

        theWord.setWordId("WD_ID_SET_By_USER");
        wordLogic.createWord(theWord);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getWordBySpelling_spellingIsNull_throwsException() {

        wordLogic.getWordBySpelling(null);
    }

    @Test
    public void getWordBySpelling_foundCached_doNotCallDB() {

        when(mockWordCache.getWordBySpelling(wordSpelling)).thenReturn(theWord);

        wordLogic.getWordBySpelling(wordSpelling);

        verify(mockWordDao, never()).getWordBySpelling(anyString());
        verify(mockWordCache, never()).cacheWord(any(Word.class));
    }

    @Test
    public void getWordBySpelling_notCached_callDatabaseAndCache() {

        when(mockWordCache.getWordBySpelling(wordSpelling)).thenReturn(null);
        when(mockWordDao.getWordBySpelling(wordSpelling)).thenReturn(theWord);

        wordLogic.getWordBySpelling(wordSpelling);

        verify(mockWordDao, times(1) ).getWordBySpelling(wordSpelling);
        verify(mockWordCache, times(1) ).cacheWord(theWord);
    }

    @Test (expected = IllegalArgumentException.class) @Ignore
    public void updateWord_nullWordId_throwsIllegalArgumentException() {

    }

    @Test (expected = IllegalArgumentException.class) @Ignore
    public void updateWord_illegalWordId_throwsIllegalArgumentException() {

    }

    @Test (expected = IllegalArgumentException.class) @Ignore
    public void updateWord_wordIdsDoNotMatch_throwsIllegalArgumentException() {

    }

    @Test @Ignore
    public void updateWord_wordIdSetWordsIdNotSet_addsWordCurrently() {

    }
}
