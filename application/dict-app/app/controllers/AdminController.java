package controllers;

import common.store.MongoStoreFactory;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import utilities.Constants;
import utilities.DictUtil;
import utilities.ShobdoLogger;
import utilities.TestUtil;
import word.WordCache;
import word.WordLogic;
import word.WordStoreMongoImpl;
import word.objects.Meaning;
import word.objects.Word;

import java.util.*;
import java.util.stream.Collectors;

public class AdminController extends Controller {

    private static WordLogic wordLogic;
    private static final ShobdoLogger logger = new ShobdoLogger(AdminController.class);

    public AdminController() {
        WordStoreMongoImpl wordStoreMongo = new WordStoreMongoImpl(MongoStoreFactory.getWordCollection());
        wordLogic = new WordLogic(wordStoreMongo, WordCache.getCache());
    }

    public Result flushCache() {
        logger.info("Flushing cache!");
        wordLogic.flushCache();
        return ok();
    }

    //TODO remove before eventual deployment
    @BodyParser.Of(BodyParser.Json.class)
    public Result createRandomDictionary() {

        final String transactionId = request().getHeader(Headers.X_TRANSACTION_ID);
        final String requestId = request().getHeader(Headers.X_REQUEST_ID);

        return ControllerUtils.executeEndpoint(transactionId, requestId, "listMeanings", new HashMap<>(),
            () -> {

                Set<String> wordSpellingSet = new HashSet<>();
                Set<Word> words = new HashSet<>();

                final int wordCount = Integer.parseInt(request().body().asJson().get(Constants.KEY_WORD_COUNT).asText());
                logger.info("Total word creation requested:" + wordCount);

                for (int count = 0; count < wordCount; count++) {
                    int numOfTries = 100;
                    for (int tryCount = 0; tryCount < numOfTries; tryCount++) {
                        Word word = TestUtil.generateRandomWord();
                        if (wordSpellingSet.contains(word.getSpelling())) {
                            continue;
                        }
                        wordSpellingSet.add(word.getSpelling());
                        words.add(word);
                        break;
                    }
                }

                logger.info("Total words for be created:" + words.size());

                List<Word> createdWords = words.stream()
                    .map( w -> {
                            try {
                                return wordLogic.createWord(w);
                            } catch (Exception ex) {
                                return null;
                            }
                        }
                    )
                    .filter(w -> w != null)
                    .collect(Collectors.toList());

                logger.info("Total words created:" + createdWords.size());

                List<Meaning> allMeanings = new ArrayList<>();

                for (Word word: createdWords) {
                    allMeanings.addAll(
                        TestUtil.generateMeanings(word.getSpelling(), DictUtil.randIntInRange(1, 5))
                            .stream()
                            .map(meaning -> wordLogic.createMeaning(word.getId(), meaning))
                            .collect(Collectors.toList())
                    );
                }

                logger.info("Total meanings created:" + allMeanings.size());

                return ok(String.format("Generated and added %s random words, %s meanings on the dictionary!",
                    createdWords.size(), allMeanings.size()));
            }
        );
    }
}
