package integration;

import com.fasterxml.jackson.databind.JsonNode;
import common.stores.MongoStoreFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import utilities.Constants;
import utilities.JsonUtil;
import utilities.ShobdoLogger;
import utilities.TestUtil;
import word.WordLogic;
import word.caches.WordCache;
import word.objects.Inflection;
import word.objects.InflectionIndex;
import word.objects.Word;
import word.stores.WordStoreMongoImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static play.test.Helpers.*;

public class InflectionControllerTests extends WithApplication {

    private final ShobdoLogger log;
    private final WordLogic wordLogic;
    private final ArrayList<Word> createdWords;

    public InflectionControllerTests() {
        log = new ShobdoLogger(InflectionControllerTests.class);
        WordStoreMongoImpl storeMongo = new WordStoreMongoImpl(
            MongoStoreFactory.getWordCollection(),
            MongoStoreFactory.getInflectionIndexCollection()
        );
        wordLogic = new WordLogic(storeMongo, WordCache.getCache());
        createdWords = new ArrayList<>();
    }

    @Before
    public void setup() {
    }

    @After
    public void clearSetups() {
        createdWords.clear();
        wordLogic.deleteAllWords();
        wordLogic.deleteAllInflectionIndexEntries();
        wordLogic.flushCache();
    }

    private Word createWord(final String spelling) {
        final Word word = Word.builder().spelling(spelling).build();
        final Word created = wordLogic.createWord(word);
        createdWords.add(created);
        return created;
    }

    // ─── PUT /api/v1/words/:id/inflections ──────────────────────────────────

    @Test
    public void addInflections_validInflections_wordUpdatedAndInflectionIndexCreated() {
        Helpers.running(Helpers.fakeApplication(), () -> {
            final Word root = createWord("আলোচনা");

            final String body = "["
                + "{\"spelling\":\"আলোচনায়\",\"type\":\"অধিকরণ\",\"meaning\":\"আলোচনায় থাকা\","
                + " \"synonyms\":[],\"antonyms\":[]},"
                + "{\"spelling\":\"আলোচনার\",\"type\":\"সম্বন্ধ\",\"meaning\":\"আলোচনার বিষয়\","
                + " \"synonyms\":[],\"antonyms\":[]}"
                + "]";
            final JsonNode bodyJson = JsonUtil.jStringToJNode(body);

            final Result result = Helpers.route(
                Helpers.fakeRequest(PUT, "/api/v1/words/" + root.getId() + "/inflections")
                    .bodyJson(bodyJson));

            Assert.assertEquals(OK, result.status());

            // returned body is the updated word
            final JsonNode wordJson = JsonUtil.jStringToJNode(contentAsString(result));
            Assert.assertNotNull(wordJson.get("inflections"));
            Assert.assertEquals(2, wordJson.get("inflections").size());

            // InflectionIndex entries exist
            final InflectionIndex idx1 = wordLogic.findInflectionBySpelling("আলোচনায়");
            Assert.assertNotNull(idx1);
            Assert.assertEquals(root.getId(), idx1.getRootId());
            Assert.assertEquals("আলোচনা", idx1.getRootSpelling());

            final InflectionIndex idx2 = wordLogic.findInflectionBySpelling("আলোচনার");
            Assert.assertNotNull(idx2);
            Assert.assertEquals(root.getId(), idx2.getRootId());
        });
    }

    @Test
    public void addInflections_invalidWordId_returnsNotFound() {
        Helpers.running(Helpers.fakeApplication(), () -> {
            final String body = "[{\"spelling\":\"আলোচনায়\",\"type\":\"অধিকরণ\",\"meaning\":\"test\","
                + "\"synonyms\":[],\"antonyms\":[]}]";
            final JsonNode bodyJson = JsonUtil.jStringToJNode(body);

            final Result result = Helpers.route(
                Helpers.fakeRequest(PUT, "/api/v1/words/invalidWordId/inflections")
                    .bodyJson(bodyJson));

            Assert.assertEquals(NOT_FOUND, result.status());
            Assert.assertEquals(Constants.Messages.EntityNotFound("invalidWordId"), contentAsString(result));
        });
    }

    @Test
    public void addInflections_nonArrayBody_returnsBadRequest() {
        Helpers.running(Helpers.fakeApplication(), () -> {
            final Word root = createWord("পরীক্ষা");

            final JsonNode bodyJson = JsonUtil.jStringToJNode("{\"spelling\":\"পরীক্ষায়\"}");
            final Result result = Helpers.route(
                Helpers.fakeRequest(PUT, "/api/v1/words/" + root.getId() + "/inflections")
                    .bodyJson(bodyJson));

            Assert.assertEquals(BAD_REQUEST, result.status());
        });
    }

    @Test
    public void addInflections_emptyArray_returnsBadRequest() {
        Helpers.running(Helpers.fakeApplication(), () -> {
            final Word root = createWord("পরীক্ষা");

            final JsonNode bodyJson = JsonUtil.jStringToJNode("[]");
            final Result result = Helpers.route(
                Helpers.fakeRequest(PUT, "/api/v1/words/" + root.getId() + "/inflections")
                    .bodyJson(bodyJson));

            Assert.assertEquals(BAD_REQUEST, result.status());
        });
    }

    @Test
    public void addInflections_duplicateInflectionSpelling_returnsBadRequest() {
        Helpers.running(Helpers.fakeApplication(), () -> {
            final Word root = createWord("আলোচনা");

            // add first time
            final String body = "[{\"spelling\":\"আলোচনায়\",\"type\":\"অধিকরণ\",\"meaning\":\"test\","
                + "\"synonyms\":[],\"antonyms\":[]}]";
            final JsonNode bodyJson = JsonUtil.jStringToJNode(body);
            final Result first = Helpers.route(
                Helpers.fakeRequest(PUT, "/api/v1/words/" + root.getId() + "/inflections")
                    .bodyJson(bodyJson));
            Assert.assertEquals(OK, first.status());

            // add same spelling again — should fail
            final Result second = Helpers.route(
                Helpers.fakeRequest(PUT, "/api/v1/words/" + root.getId() + "/inflections")
                    .bodyJson(bodyJson));
            Assert.assertEquals(BAD_REQUEST, second.status());
        });
    }

    @Test
    public void addInflections_missingSpelling_returnsBadRequest() {
        Helpers.running(Helpers.fakeApplication(), () -> {
            final Word root = createWord("পরীক্ষা");

            // spelling field is missing/empty
            final JsonNode bodyJson = JsonUtil.jStringToJNode(
                "[{\"spelling\":\"\",\"type\":\"অধিকরণ\",\"meaning\":\"test\"}]");
            final Result result = Helpers.route(
                Helpers.fakeRequest(PUT, "/api/v1/words/" + root.getId() + "/inflections")
                    .bodyJson(bodyJson));

            Assert.assertEquals(BAD_REQUEST, result.status());
        });
    }

    // ─── createWord guard: spelling is a known inflection ───────────────────

    @Test
    public void createWord_spellingIsKnownInflection_returnsBadRequest() {
        Helpers.running(Helpers.fakeApplication(), () -> {
            final Word root = createWord("আলোচনা");

            // register an inflection
            final String inflBody = "[{\"spelling\":\"আলোচনায়\",\"type\":\"অধিকরণ\",\"meaning\":\"test\","
                + "\"synonyms\":[],\"antonyms\":[]}]";
            Helpers.route(
                Helpers.fakeRequest(PUT, "/api/v1/words/" + root.getId() + "/inflections")
                    .bodyJson(JsonUtil.jStringToJNode(inflBody)));

            // now try to create a root word with the inflected spelling
            final String createBody = "{\"id\":null,\"spelling\":\"আলোচনায়\",\"meanings\":{}}";
            final Result result = Helpers.route(
                Helpers.fakeRequest(POST, "/api/v1/words")
                    .bodyJson(JsonUtil.jStringToJNode(createBody)));

            Assert.assertEquals(BAD_REQUEST, result.status());
            Assert.assertEquals(
                Constants.Messages.SpellingIsInflection("আলোচনায়"),
                contentAsString(result));
        });
    }

    // ─── GET /api/v1/bn/word/:spelling inflection fallback ──────────────────

    @Test
    public void getWordByLangAndSpelling_inflectedSpelling_returnsRootWordWithInflectedFromField() {
        Helpers.running(Helpers.fakeApplication(), () -> {
            final Word root = createWord("আলোচনা");

            // register the inflection
            final String inflBody = "[{\"spelling\":\"আলোচনায়\",\"type\":\"অধিকরণ\",\"meaning\":\"আলোচনায় থাকা\","
                + "\"synonyms\":[],\"antonyms\":[]}]";
            Helpers.route(
                Helpers.fakeRequest(PUT, "/api/v1/words/" + root.getId() + "/inflections")
                    .bodyJson(JsonUtil.jStringToJNode(inflBody)));

            // fetch by the inflected spelling
            final Result result = Helpers.route(
                Helpers.fakeRequest(GET, "/api/v1/bn/word/আলোচনায়"));
            Assert.assertEquals(OK, result.status());

            final JsonNode json = JsonUtil.jStringToJNode(contentAsString(result));
            // root word is returned
            Assert.assertEquals(root.getId(), json.get("id").asText());
            Assert.assertEquals("আলোচনা", json.get("spelling").asText());
            // inflectedFrom field is present
            Assert.assertNotNull(json.get("inflectedFrom"));
            Assert.assertEquals("আলোচনায়", json.get("inflectedFrom").asText());
        });
    }

    @Test
    public void getWordByLangAndSpelling_rootSpelling_returnsWordWithoutInflectedFromField() {
        Helpers.running(Helpers.fakeApplication(), () -> {
            final Word root = createWord("আলোচনা");

            final Result result = Helpers.route(
                Helpers.fakeRequest(GET, "/api/v1/bn/word/আলোচনা"));
            Assert.assertEquals(OK, result.status());

            final JsonNode json = JsonUtil.jStringToJNode(contentAsString(result));
            Assert.assertEquals(root.getId(), json.get("id").asText());
            // no inflectedFrom field for a direct word lookup
            Assert.assertNull(json.get("inflectedFrom"));
        });
    }

    @Test
    public void getWordByLangAndSpelling_spellingNotFoundAndNotInflection_returnsNotFound() {
        Helpers.running(Helpers.fakeApplication(), () -> {
            final Result result = Helpers.route(
                Helpers.fakeRequest(GET, "/api/v1/bn/word/nonexistentword"));
            Assert.assertEquals(NOT_FOUND, result.status());
        });
    }
}
