package unit;

import word.caches.WordCache;
import word.stores.WordStore;
import word.WordLogic;
import word.objects.Word;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.mockito.Mockito.*;

public class WordLogicTest {

    public Logger log = LoggerFactory.getLogger(WordLogicTest.class);

    private WordLogic wordLogic;
    private WordStore mockWordStore;
    private WordCache mockWordCache;

    private String spelling = "পিটন";
    private Word theWord;

    private void setupMocks() {
        mockWordStore = mock(WordStore.class);
        mockWordCache = mock(WordCache.class);
        wordLogic = new WordLogic(mockWordStore, mockWordCache);
    }

    private void setupObjects() {
        theWord = Word.builder()
            .spelling(spelling)
            .build();
    }

    @Before
    public void setup() {
        setupMocks();
        setupObjects();
    }

    //Create word
    @Test
    public void createWord_wordIdIsNotSet_createWordDaoCalled() {
        when(mockWordStore.create(any())).thenReturn(theWord);
        when(mockWordStore.getById(anyString())).thenReturn(null);
        wordLogic.createWord(theWord);
        verify(mockWordStore, times(1)).create(any(Word.class));
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
        verify(mockWordStore, never()).getBySpelling(anyString());
        verify(mockWordCache, never()).cacheBySpelling(any(Word.class));
    }

    @Test
    public void getWordBySpelling_notCached_callDatabaseAndCache() throws IOException {
        when(mockWordCache.getBySpelling(spelling)).thenReturn(null);
        when(mockWordStore.getBySpelling(spelling)).thenReturn(theWord);
        wordLogic.getWordBySpelling(spelling);
        verify(mockWordStore, times(1) ).getBySpelling(spelling);
        verify(mockWordCache, times(1) ).cacheBySpelling(theWord);
    }
}
