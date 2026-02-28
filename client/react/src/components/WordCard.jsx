import LikeButton from './LikeButton';

export default function WordCard({ word, isActive, onSelect }) {
  return (
    <li
      className={`word-card${isActive ? ' active' : ''}`}
      onClick={() => onSelect(word)}
    >
      <div className="wc-word">{word.spelling}</div>
      <LikeButton wordId={word.id} />
    </li>
  );
}
