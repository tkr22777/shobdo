package like.stores;

import like.objects.Like;

import java.util.List;

public interface LikeStore {
    Like getLike(String userId, String wordId);
    Like createLike(Like like);
    void deleteLike(String userId, String wordId);
    List<String> getLikedWordIds(String userId);
    long countLikes(String wordId);
}
