import WordCard from './WordCard';

export default function WordList({ results, query, selectedSpelling, onSelect }) {
  if (!results || results.length === 0) {
    if (query && query.trim()) {
      return (
        <ul id="wordList">
          <li className="no-results">
            <strong>কোনো ফলাফল নেই</strong>
            {'\u201C'}{query}{'\u201D'}{' \u2014 '}এই শব্দটি অভিধানে পাওয়া যায়নি।
          </li>
        </ul>
      );
    }
    return <ul id="wordList"></ul>;
  }

  return (
    <ul id="wordList">
      {results.map(word => (
        <WordCard
          key={word.id}
          word={word}
          isActive={word.spelling === selectedSpelling}
          onSelect={onSelect}
        />
      ))}
    </ul>
  );
}
