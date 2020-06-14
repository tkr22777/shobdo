package unit;

import caches.WordCache;
import daos.WordDao;
import logics.WordLogic;
import objects.Word;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import utilities.ShobdoLogger;

import java.io.IOException;

import static org.mockito.Mockito.*;
/**
 *
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
public class WordLogicTest {

    public ShobdoLogger log = new ShobdoLogger(WordLogicTest.class);

    private WordDao mockWordDao;
    private WordCache mockWordCache;

    private WordLogic wordLogic;

    private String spelling = "পিটন";
    private Word theWord;

    @Before
    public void setup() {
        setupMocks();
        setupObjects();
    }

    private void setupMocks() {
        mockWordDao = mock(WordDao.class);
        mockWordCache = mock(WordCache.class);
        wordLogic = new WordLogic(mockWordDao, mockWordCache);
    }

    private void setupObjects() {
        theWord = Word.builder()
            .spelling(spelling)
            .build();
    }

    //Create word
    @Ignore @Test
    //TODO
    public void generateWordId_wordIdExists_throwsRuntimeException() {
    }

    @Test
    public void createWord_wordIdIsNotSet_createWordDaoCalled() {
        when(mockWordDao.create(any())).thenReturn(theWord);
        when(mockWordDao.getById(anyString())).thenReturn(null);
        wordLogic.createWord(theWord);
        verify(mockWordDao, times(1)).create(any(Word.class));
        verify(mockWordCache, times(1)).cacheBySpelling(any(Word.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createWord_wordIdIsSet_throwsIAE() {
        theWord.setId("WD_ID_SET");
        wordLogic.createWord(theWord);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getWordBySpelling_spellingIsNull_throwsException() throws IOException {
        wordLogic.getWordBySpelling(null);
    }

    @Test
    public void getWordBySpelling_foundCached_doNotCallDB() throws IOException {
        when(mockWordCache.getBySpelling(spelling)).thenReturn(theWord);
        wordLogic.getWordBySpelling(spelling);
        verify(mockWordDao, never()).getBySpelling(anyString());
        verify(mockWordCache, never()).cacheBySpelling(any(Word.class));
    }

    @Test
    public void getWordBySpelling_notCached_callDatabaseAndCache() throws IOException {
        when(mockWordCache.getBySpelling(spelling)).thenReturn(null);
        when(mockWordDao.getBySpelling(spelling)).thenReturn(theWord);
        wordLogic.getWordBySpelling(spelling);
        verify(mockWordDao, times(1) ).getBySpelling(spelling);
        verify(mockWordCache, times(1) ).cacheBySpelling(theWord);
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
