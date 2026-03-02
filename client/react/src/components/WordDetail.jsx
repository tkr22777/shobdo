import { useEffect, useState, useRef } from 'react';
import { useLikes } from '../context/LikeContext';
import LikeButton from './LikeButton';

function getBengaliDigit(n) {
  const zero = '০'.charCodeAt(0);
  return n.toString().split('').map(d => String.fromCharCode(zero + parseInt(d))).join('');
}

function highlightWord(sentence, word, onWordClick) {
  if (!sentence) return null;
  const parts = sentence.split(/(\s+|[।,!?])/g);
  return parts.map((part, i) => {
    if (word && part.includes(word)) {
      return (
        <span key={i} className="highlighted-word" onClick={() => onWordClick(part.trim())}>
          {part}
        </span>
      );
    }
    return part;
  });
}

const ShareSVG = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24"
    fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"
    strokeLinejoin="round" aria-hidden="true">
    <circle cx="18" cy="5" r="3" /><circle cx="6" cy="12" r="3" /><circle cx="18" cy="19" r="3" />
    <line x1="8.59" y1="13.51" x2="15.42" y2="17.49" />
    <line x1="15.41" y1="6.51" x2="8.59" y2="10.49" />
  </svg>
);

const CheckSVG = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24"
    fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round"
    strokeLinejoin="round" aria-hidden="true">
    <polyline points="20 6 9 17 4 12" />
  </svg>
);

function EmptyState() {
  return (
    <div className="empty-state">
      <div className="empty-ornament">শ</div>
      <p>একটি শব্দ খুঁজুন<br />
        <span style={{ opacity: 0.6, fontSize: '0.85em' }}>Type a word to begin</span>
      </p>
    </div>
  );
}

function AboutPage() {
  return (
    <div className="about-article">
      <div className="article-kicker">ABOUT</div>
      <h1 className="article-headline">পরিচিতি</h1>
      <div className="article-deck">বাংলা ভাষার শব্দ ও অর্থের সন্ধানে।</div>
      <div className="article-byline"><span>শব্দ টিম · MMXXIV</span></div>
      <div className="article-body">
        <p className="def-graf">শব্দ একটি বাংলা অভিধান অ্যাপ্লিকেশন। এটি বাংলা শব্দের অর্থ, প্রয়োগ ও ব্যাকরণগত বৈশিষ্ট্য খুঁজে পেতে সাহায্য করে।</p>
      </div>
    </div>
  );
}

export default function WordDetail({ data, viewMode, onTagClick }) {
  const { fetchLikeCount } = useLikes();
  const [shareCopied, setShareCopied] = useState(false);
  const articleRef = useRef(null);

  useEffect(() => {
    if (data?.id) {
      fetchLikeCount(data.id);
    }
    // Scroll to top when a new word is loaded
    if (articleRef.current) {
      articleRef.current.scrollTop = 0;
    }
  }, [data?.id]);

  const handleShare = () => {
    if (!data?.spelling) return;
    const url = new URL(window.location.origin);
    url.pathname = `/bn/word/${encodeURIComponent(data.spelling)}`;
    navigator.clipboard.writeText(url.toString()).catch(() => {});
    setShareCopied(true);
    setTimeout(() => setShareCopied(false), 2000);
  };

  let content;
  if (viewMode === 'about') {
    content = <AboutPage />;
  } else if (!data) {
    content = <EmptyState />;
  } else {
    const meanings = data.meanings || {};
    const entries = Object.entries(meanings);
    const totalMeanings = entries.length;
    const firstMeaning = entries[0]?.[1];
    const deckText = firstMeaning?.text || '';

    const allSynonyms = [];
    entries.forEach(([, meaning]) => {
      if (Array.isArray(meaning.synonyms)) allSynonyms.push(...meaning.synonyms);
    });
    const uniqueSynonyms = [...new Set(allSynonyms)].slice(0, 8);

    content = (
      <div className="article">
        <h1 className="article-headline">{data.spelling}</h1>
        {deckText && <div className="article-deck">{deckText}</div>}
        <div className="article-byline" data-word-id={data.id}>
          <span>বাংলা</span>
          <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
            <button className="meaning-share-btn" onClick={handleShare}>
              {shareCopied ? <><CheckSVG /> কপি হয়েছে</> : <><ShareSVG /> শেয়ার</>}
            </button>
            <LikeButton wordId={data.id} />
          </div>
        </div>
        <div className="article-body">
          {entries.map(([key, meaning], index) => {
            const isFirst = index === 0;
            const hasSynonyms = Array.isArray(meaning.synonyms) && meaning.synonyms.length > 0;
            const hasAntonyms = Array.isArray(meaning.antonyms) && meaning.antonyms.length > 0;
            const hasExample = !!meaning.exampleSentence;

            return (
              <div key={key}>
                <p className="def-graf">
                  {totalMeanings > 1 && (
                    <span className="meaning-number">{getBengaliDigit(index + 1)}.</span>
                  )}{' '}
                  {meaning.text}
                </p>
                {(hasSynonyms || hasAntonyms) && (
                  <p className="example-graf" style={{ fontStyle: 'normal' }}>
                    {hasSynonyms && <em>সমার্থ: {meaning.synonyms.join(', ')}</em>}
                    {hasSynonyms && hasAntonyms && <span> &nbsp;·&nbsp; </span>}
                    {hasAntonyms && <em>বিপরীত: {meaning.antonyms.join(', ')}</em>}
                  </p>
                )}
                {hasExample && (
                  <p className="example-graf">
                    {highlightWord(meaning.exampleSentence, data.spelling, onTagClick)}
                  </p>
                )}
                {isFirst && totalMeanings > 1 && hasExample && (
                  <div className="pull-quote">
                    &ldquo;{meaning.exampleSentence}&rdquo;
                  </div>
                )}
              </div>
            );
          })}
        </div>
        {uniqueSynonyms.length > 0 && (
          <div className="article-footer">
            <div className="footer-label">সম্পর্কিত শব্দ</div>
            <div className="word-tags">
              {uniqueSynonyms.map(s => (
                <span
                  key={s}
                  className="word-tag"
                  onClick={() => onTagClick(s)}
                >
                  {s}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>
    );
  }

  return (
    <article id="wordMeaning" ref={articleRef}>
      {content}
    </article>
  );
}
