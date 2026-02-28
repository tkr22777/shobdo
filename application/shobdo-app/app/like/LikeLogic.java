package like;

import like.objects.Like;
import like.stores.LikeStore;
import utilities.ShobdoLogger;

import java.util.List;
import java.util.UUID;

public class LikeLogic {

    private static final String PREFIX_LIKE_ID = "LIK";
    private static final ShobdoLogger log = new ShobdoLogger(LikeLogic.class);

    private final LikeStore likeStore;

    public LikeLogic(final LikeStore likeStore) {
        this.likeStore = likeStore;
    }

    public boolean toggleLike(final String userId, final String wordId) {
        final Like existing = likeStore.getLike(userId, wordId);
        if (existing == null) {
            final String likeId = String.format("%s-%s", PREFIX_LIKE_ID, UUID.randomUUID());
            final Like like = Like.builder()
                .id(likeId)
                .userId(userId)
                .wordId(wordId)
                .build();
            likeStore.createLike(like);
            log.debug("toggleLike created like id:" + likeId);
            return true;
        } else {
            likeStore.deleteLike(userId, wordId);
            log.debug("toggleLike removed like for userId:" + userId + " wordId:" + wordId);
            return false;
        }
    }

    public void removeLike(final String userId, final String wordId) {
        final Like existing = likeStore.getLike(userId, wordId);
        if (existing != null) {
            likeStore.deleteLike(userId, wordId);
            log.debug("removeLike userId:" + userId + " wordId:" + wordId);
        }
    }

    public List<String> getLikedWordIds(final String userId) {
        return likeStore.getLikedWordIds(userId);
    }

    public long getLikeCount(final String wordId) {
        return likeStore.countLikes(wordId);
    }
}
