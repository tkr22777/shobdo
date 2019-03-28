package unitTests;

import caches.WordCache;
import daos.WordDao;
import logics.WordLogic;
import objects.Word;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import utilities.LogPrint;

import java.io.IOException;

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
        theWord = Word.builder()
            .wordSpelling(wordSpelling)
            .build();
    }

    //Create word
    @Test
    public void createWord_wordIdIsNotSet_createWordDaoCalled() {
        when(mockWordDao.create(any())).thenReturn(theWord);
        wordLogic.createWord(theWord);
        verify(mockWordDao, times(1)).create(any(Word.class));
        verify(mockWordCache, times(1)).cacheWord(any(Word.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWord_wordIdIsSet_throwsIAE() {

        theWord.setId("WD_ID_SET_By_USER");
        wordLogic.createWord(theWord);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getWordBySpelling_spellingIsNull_throwsException() throws IOException {
        wordLogic.getWordBySpelling(null);
    }

    @Test
    public void getWordBySpelling_foundCached_doNotCallDB() throws IOException {
        when(mockWordCache.getBySpelling(wordSpelling)).thenReturn(theWord);
        wordLogic.getWordBySpelling(wordSpelling);
        verify(mockWordDao, never()).getBySpelling(anyString());
        verify(mockWordCache, never()).cacheWord(any(Word.class));
    }

    @Test
    public void getWordBySpelling_notCached_callDatabaseAndCache() throws IOException {
        when(mockWordCache.getBySpelling(wordSpelling)).thenReturn(null);
        when(mockWordDao.getBySpelling(wordSpelling)).thenReturn(theWord);
        wordLogic.getWordBySpelling(wordSpelling);
        verify(mockWordDao, times(1) ).getBySpelling(wordSpelling);
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
