package unit;

import exceptions.EntityDoesNotExist;
import word.caches.RandomWordPool;
import word.caches.WordCache;
import word.stores.WordStore;
import word.WordLogic;
import word.objects.Inflection;
import word.objects.InflectionIndex;
import word.objects.Word;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import utilities.Constants;
import utilities.ShobdoLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class WordLogicTest {

    public ShobdoLogger log = new ShobdoLogger(WordLogicTest.class);

    private WordLogic wordLogic;
    private WordStore mockWordStore;
    private WordCache mockWordCache;

    private String spelling = "পিটন";
    private Word theWord;

    private static final String WORD_ID = "WD-test-id";
    private static final String INF_SPELLING = "পিটনে";
    private static final String INF_SPELLING_2 = "পিটনের";

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

    // ─── createWord: inflection guard ────────────────────────────────────────

    @Test(expected = IllegalArgumentException.class)
    public void createWord_spellingIsKnownInflection_throwsIAE() {
        when(mockWordStore.getBySpelling(spelling)).thenReturn(null);
        when(mockWordStore.findInflectionBySpelling(spelling)).thenReturn(
            InflectionIndex.builder().spelling(spelling).rootId("WD-other").rootSpelling("other").build()
        );
        wordLogic.createWord(theWord);
    }

    @Test
    public void createWord_spellingIsKnownInflection_errorMessageContainsSpelling() {
        when(mockWordStore.getBySpelling(spelling)).thenReturn(null);
        when(mockWordStore.findInflectionBySpelling(spelling)).thenReturn(
            InflectionIndex.builder().spelling(spelling).rootId("WD-other").rootSpelling("other").build()
        );
        try {
            wordLogic.createWord(theWord);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals(Constants.Messages.SpellingIsInflection(spelling), e.getMessage());
        }
    }

    @Test
    public void createWord_spellingNotAnInflection_proceedsToCreate() {
        when(mockWordStore.getBySpelling(spelling)).thenReturn(null);
        when(mockWordStore.findInflectionBySpelling(spelling)).thenReturn(null);
        when(mockWordStore.getById(anyString())).thenReturn(null);
        when(mockWordStore.create(any())).thenReturn(theWord);

        wordLogic.createWord(theWord);

        verify(mockWordStore, times(1)).create(any(Word.class));
    }

    // ─── addInflectionsToWord ─────────────────────────────────────────────────

    @Test
    public void addInflectionsToWord_validInflections_writesToWordAndIndex() {
        final Word root = Word.builder().id(WORD_ID).spelling(spelling).build();
        when(mockWordStore.getById(WORD_ID)).thenReturn(root);
        when(mockWordStore.findInflectionBySpelling(INF_SPELLING)).thenReturn(null);
        when(mockWordStore.findInflectionBySpelling(INF_SPELLING_2)).thenReturn(null);

        final List<Inflection> inflections = Arrays.asList(
            Inflection.builder().spelling(INF_SPELLING).type("অধিকরণ").meaning("test1").build(),
            Inflection.builder().spelling(INF_SPELLING_2).type("সম্বন্ধ").meaning("test2").build()
        );

        wordLogic.addInflectionsToWord(WORD_ID, inflections);

        // Word document updated
        verify(mockWordStore, times(1)).addInflectionsToWord(WORD_ID, inflections);
        // InflectionIndex entries created — one per inflection
        verify(mockWordStore, times(2)).createInflectionIndex(any(InflectionIndex.class));
        // cache refreshed
        verify(mockWordCache, times(1)).cacheBySpelling(any(Word.class));
    }

    @Test
    public void addInflectionsToWord_indexEntryHasCorrectRootData() {
        final Word root = Word.builder().id(WORD_ID).spelling(spelling).build();
        when(mockWordStore.getById(WORD_ID)).thenReturn(root);
        when(mockWordStore.findInflectionBySpelling(INF_SPELLING)).thenReturn(null);

        wordLogic.addInflectionsToWord(WORD_ID,
            Collections.singletonList(
                Inflection.builder().spelling(INF_SPELLING).type("অধিকরণ").meaning("test").build()
            )
        );

        verify(mockWordStore).createInflectionIndex(argThat(idx ->
            INF_SPELLING.equals(idx.getSpelling())
                && WORD_ID.equals(idx.getRootId())
                && spelling.equals(idx.getRootSpelling())
                && idx.getId() != null
        ));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addInflectionsToWord_emptyList_throwsIAE() {
        wordLogic.addInflectionsToWord(WORD_ID, Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addInflectionsToWord_nullList_throwsIAE() {
        wordLogic.addInflectionsToWord(WORD_ID, null);
    }

    @Test(expected = EntityDoesNotExist.class)
    public void addInflectionsToWord_wordNotFound_throwsEntityDoesNotExist() {
        when(mockWordStore.getById(WORD_ID)).thenReturn(null);
        wordLogic.addInflectionsToWord(WORD_ID,
            Collections.singletonList(
                Inflection.builder().spelling(INF_SPELLING).type("অধিকরণ").meaning("test").build()
            )
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void addInflectionsToWord_emptySpelling_throwsIAE() {
        final Word root = Word.builder().id(WORD_ID).spelling(spelling).build();
        when(mockWordStore.getById(WORD_ID)).thenReturn(root);
        wordLogic.addInflectionsToWord(WORD_ID,
            Collections.singletonList(
                Inflection.builder().spelling("").type("অধিকরণ").meaning("test").build()
            )
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void addInflectionsToWord_duplicateSpellingInIndex_throwsIAE() {
        final Word root = Word.builder().id(WORD_ID).spelling(spelling).build();
        when(mockWordStore.getById(WORD_ID)).thenReturn(root);
        when(mockWordStore.findInflectionBySpelling(INF_SPELLING)).thenReturn(
            InflectionIndex.builder().spelling(INF_SPELLING).rootId(WORD_ID).rootSpelling(spelling).build()
        );
        wordLogic.addInflectionsToWord(WORD_ID,
            Collections.singletonList(
                Inflection.builder().spelling(INF_SPELLING).type("অধিকরণ").meaning("test").build()
            )
        );
    }

    @Test
    public void addInflectionsToWord_duplicateSpelling_neitherWriteOccurs() {
        final Word root = Word.builder().id(WORD_ID).spelling(spelling).build();
        when(mockWordStore.getById(WORD_ID)).thenReturn(root);
        when(mockWordStore.findInflectionBySpelling(INF_SPELLING)).thenReturn(
            InflectionIndex.builder().spelling(INF_SPELLING).rootId(WORD_ID).rootSpelling(spelling).build()
        );
        try {
            wordLogic.addInflectionsToWord(WORD_ID,
                Collections.singletonList(
                    Inflection.builder().spelling(INF_SPELLING).type("অধিকরণ").meaning("test").build()
                )
            );
        } catch (IllegalArgumentException ignored) {}

        verify(mockWordStore, never()).addInflectionsToWord(anyString(), anyList());
        verify(mockWordStore, never()).createInflectionIndex(any());
    }

    // ─── getRandomWord / fillRandomWordPool ───────────────────────────────────

    @Test
    public void getRandomWord_poolReady_servesFromPool() {
        final RandomWordPool mockPool = mock(RandomWordPool.class);
        when(mockPool.getRandom()).thenReturn(theWord);
        final WordLogic wl = new WordLogic(mockWordStore, mockWordCache, mockPool);

        final Word result = wl.getRandomWord();

        assertEquals(theWord, result);
        verify(mockWordStore, never()).getRandomWord();
    }

    @Test
    public void getRandomWord_poolEmpty_fallsBackToStore() {
        final RandomWordPool mockPool = mock(RandomWordPool.class);
        when(mockPool.getRandom()).thenReturn(null);
        when(mockWordStore.getRandomWord()).thenReturn(theWord);
        final WordLogic wl = new WordLogic(mockWordStore, mockWordCache, mockPool);

        final Word result = wl.getRandomWord();

        assertEquals(theWord, result);
        verify(mockWordStore, times(1)).getRandomWord();
    }

    @Test
    public void fillRandomWordPool_storeReturnsWords_poolFilled() {
        final RandomWordPool mockPool = mock(RandomWordPool.class);
        // Return POOL_SIZE words in one batch so the loop exits immediately (no sleep iterations)
        final List<Word> batch = new ArrayList<>();
        for (int i = 0; i < RandomWordPool.POOL_SIZE; i++) {
            batch.add(Word.builder().spelling("word" + i).build());
        }
        when(mockWordStore.getRandomWords(anyInt())).thenReturn(batch);
        final WordLogic wl = new WordLogic(mockWordStore, mockWordCache, mockPool);

        wl.fillRandomWordPool();

        verify(mockPool, times(1)).clear();
        verify(mockPool, atLeastOnce()).addWord(any(Word.class));
    }

    @Test
    public void fillRandomWordPool_storeThrows_doesNotPropagateException() {
        final RandomWordPool mockPool = mock(RandomWordPool.class);
        when(mockWordStore.getRandomWords(anyInt())).thenThrow(new RuntimeException("DB error"));
        final WordLogic wl = new WordLogic(mockWordStore, mockWordCache, mockPool);

        // must not throw
        wl.fillRandomWordPool();

        verify(mockPool, times(1)).clear();
        verify(mockPool, never()).addWord(any());
    }

    // ─── findInflectionBySpelling ─────────────────────────────────────────────

    @Test
    public void findInflectionBySpelling_existingSpelling_returnsIndexEntry() {
        final InflectionIndex entry = InflectionIndex.builder()
            .spelling(INF_SPELLING).rootId(WORD_ID).rootSpelling(spelling).build();
        when(mockWordStore.findInflectionBySpelling(INF_SPELLING)).thenReturn(entry);

        final InflectionIndex result = wordLogic.findInflectionBySpelling(INF_SPELLING);

        assertNotNull(result);
        assertEquals(INF_SPELLING, result.getSpelling());
        assertEquals(WORD_ID, result.getRootId());
    }

    @Test
    public void findInflectionBySpelling_unknownSpelling_returnsNull() {
        when(mockWordStore.findInflectionBySpelling(INF_SPELLING)).thenReturn(null);
        assertNull(wordLogic.findInflectionBySpelling(INF_SPELLING));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findInflectionBySpelling_nullSpelling_throwsIAE() {
        wordLogic.findInflectionBySpelling(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findInflectionBySpelling_emptySpelling_throwsIAE() {
        wordLogic.findInflectionBySpelling("");
    }
}
