import { useAuth } from '../context/AuthContext';
import { useLikes } from '../context/LikeContext';

export default function LikeButton({ wordId }) {
  const { user } = useAuth();
  const { liked, likeCounts, likeWord, unlikeWord } = useLikes();

  const isLiked = liked.has(wordId);
  const count = likeCounts.get(wordId) || 0;

  const handleClick = (e) => {
    e.stopPropagation();
    if (!user) return;
    if (isLiked) {
      unlikeWord(wordId);
    } else {
      likeWord(wordId);
    }
  };

  return (
    <>
      <button
        className={`like-btn${isLiked ? ' liked' : ''}`}
        data-word-id={wordId}
        onClick={handleClick}
        title={user ? (isLiked ? 'আনলাইক' : 'লাইক করুন') : 'সাইন ইন করুন'}
      >
        {isLiked ? '♥' : '♡'}
      </button>
      {count > 0 && (
        <span className="like-count" data-word-id={wordId}>{count}</span>
      )}
    </>
  );
}
