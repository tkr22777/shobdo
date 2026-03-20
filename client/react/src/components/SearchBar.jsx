import { useState, useRef, useEffect } from 'react';
import { getRandomWord } from '../api';

const SearchSVG = () => (
  <svg className="search-icon" width="17" height="17" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
    aria-hidden="true">
    <circle cx="11" cy="11" r="8" />
    <line x1="21" y1="21" x2="16.65" y2="16.65" />
  </svg>
);

const SurpriseSVG = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
    fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"
    strokeLinejoin="round" aria-hidden="true">
    <polyline points="16 3 21 3 21 8" /><line x1="4" y1="20" x2="21" y2="3" />
    <polyline points="21 16 21 21 16 21" /><line x1="15" y1="15" x2="21" y2="21" />
  </svg>
);

const ShareSVG = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
    fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"
    strokeLinejoin="round" aria-hidden="true">
    <circle cx="18" cy="5" r="3" />
    <circle cx="6" cy="12" r="3" />
    <circle cx="18" cy="19" r="3" />
    <line x1="8.59" y1="13.51" x2="15.42" y2="17.49" />
    <line x1="15.41" y1="6.51" x2="8.59" y2="10.49" />
  </svg>
);

const CheckSVG = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
    fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"
    strokeLinejoin="round" aria-hidden="true">
    <polyline points="20 6 9 17 4 12" />
  </svg>
);

export default function SearchBar({ value, transliterated, candidates = [], onChange, onSurprise, onShare, onCandidateSelect, onDismissCandidates, inputRef }) {
  const [copied, setCopied] = useState(false);
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const wrapRef = useRef(null);

  // Reset highlight when candidates change
  useEffect(() => {
    setHighlightedIndex(-1);
  }, [candidates]);

  // Close dropdown on outside click
  useEffect(() => {
    if (!candidates.length) return;
    const handler = (e) => {
      if (wrapRef.current && !wrapRef.current.contains(e.target)) {
        onDismissCandidates?.();
      }
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, [candidates, onDismissCandidates]);

  const handleSurprise = async () => {
    try {
      const data = await getRandomWord();
      if (data?.spelling) onSurprise(data);
    } catch {}
  };

  const handleShare = () => {
    onShare();
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleKeyDown = (e) => {
    if (!candidates.length) return;

    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setHighlightedIndex(i => Math.min(i + 1, candidates.length - 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setHighlightedIndex(i => Math.max(i - 1, 0));
    } else if (e.key === 'Enter' && highlightedIndex >= 0) {
      e.preventDefault();
      onCandidateSelect(candidates[highlightedIndex]);
    } else if (e.key === 'Escape') {
      e.preventDefault();
      onDismissCandidates?.();
    }
  };

  const showDropdown = candidates.length > 1;

  return (
    <div className="search-section">
      <div className="search-wrap" ref={wrapRef}>
        <SearchSVG />
        <label htmlFor="wordSearchBox" className="sr-only">শব্দ খুঁজুন</label>
        <input
          type="text"
          id="wordSearchBox"
          ref={inputRef}
          value={value}
          onChange={e => onChange(e.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="শব্দ খুঁজুন..."
          autoComplete="off"
          aria-autocomplete="list"
          aria-expanded={showDropdown}
          aria-haspopup="listbox"
        />
        <button
          id="surpriseBtn"
          className="share-btn surprise-btn"
          title="এলোমেলো শব্দ"
          aria-label="এলোমেলো শব্দ"
          style={{ right: '46px' }}
          onClick={handleSurprise}
        >
          <SurpriseSVG />
        </button>
        <button
          id="shareButton"
          className="share-btn"
          title="শেয়ার করুন"
          aria-label="শেয়ার করুন"
          onClick={handleShare}
        >
          {copied ? <CheckSVG /> : <ShareSVG />}
        </button>
        {showDropdown && (
          <ul className="search-suggestions" role="listbox" aria-label="বানানের বিকল্পসমূহ">
            {candidates.map((candidate, idx) => (
              <li
                key={candidate}
                role="option"
                aria-selected={idx === highlightedIndex}
                className={`suggestion-item${idx === highlightedIndex ? ' highlighted' : ''}`}
                onMouseEnter={() => setHighlightedIndex(idx)}
                onMouseLeave={() => setHighlightedIndex(-1)}
                onMouseDown={(e) => {
                  e.preventDefault(); // prevent input blur
                  onCandidateSelect(candidate);
                }}
              >
                <SearchSVG />
                {candidate}
              </li>
            ))}
          </ul>
        )}
      </div>
      <div className="transliteration-container">
        <span
          id="transliteratedText"
          className="transliterated-label"
          style={{
            opacity: transliterated ? 1 : 0,
            visibility: transliterated ? 'visible' : 'hidden',
          }}
        >
          {transliterated}
        </span>
      </div>
    </div>
  );
}
