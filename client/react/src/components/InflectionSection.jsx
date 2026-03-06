import { useState, useEffect } from 'react';

export default function InflectionSection({ inflections, inflectedFrom, onTagClick }) {
  const [activeKey, setActiveKey] = useState(null);
  const [bubble, setBubble] = useState(null); // { x, y, inflection }

  // Auto-expand the inflection that was redirected from
  useEffect(() => {
    if (!inflectedFrom || !inflections?.length) {
      setActiveKey(null);
      return;
    }
    const match = inflections.find(inf => inf.spelling === inflectedFrom);
    if (match) setActiveKey(match.spelling);
  }, [inflectedFrom, inflections]);

  if (!inflections?.length) return null;

  const activeInflection = inflections.find(inf => inf.spelling === activeKey) || null;

  function handleTagClick(inf) {
    setActiveKey(prev => (prev === inf.spelling ? null : inf.spelling));
  }

  function handleMouseEnter(e, inf) {
    if (!inf.meaning && !inf.type) return;
    const r = e.currentTarget.getBoundingClientRect();
    setBubble({ x: r.left + r.width / 2, y: r.top - 8, inflection: inf });
  }

  return (
    <div className="inflection-section">
      <div className="footer-label">রূপভেদ</div>
      <div className="inflection-tags">
        {inflections.map(inf => (
          <span
            key={inf.spelling}
            className={`inflection-tag${activeKey === inf.spelling ? ' active' : ''}`}
            onMouseEnter={e => handleMouseEnter(e, inf)}
            onMouseLeave={() => setBubble(null)}
            onClick={() => handleTagClick(inf)}
          >
            {inf.spelling}
          </span>
        ))}
      </div>

      {bubble && (
        <div
          className="inflection-bubble"
          style={{ left: bubble.x, top: bubble.y }}
        >
          {bubble.inflection.meaning || bubble.inflection.type}
        </div>
      )}

      {activeInflection && (
        <div className="inflection-panel">
          <div className="inflection-panel-word">{activeInflection.spelling}</div>
          {activeInflection.type && (
            <div className="inflection-type-badge">{activeInflection.type}</div>
          )}
          {activeInflection.meaning && (
            <p className="inflection-panel-meaning">{activeInflection.meaning}</p>
          )}
          {activeInflection.exampleSentence && (
            <p className="example-graf">{activeInflection.exampleSentence}</p>
          )}
          {(activeInflection.synonyms?.length > 0 || activeInflection.antonyms?.length > 0) && (
            <div className="syn-ant-row">
              {activeInflection.synonyms?.length > 0 && (
                <>
                  <span className="syn-ant-label">সমার্থ</span>
                  {activeInflection.synonyms.map(s => (
                    <span key={s} className="syn-word" onClick={() => onTagClick?.(s)}>{s}</span>
                  ))}
                </>
              )}
              {activeInflection.synonyms?.length > 0 && activeInflection.antonyms?.length > 0 && (
                <span className="syn-ant-sep">·</span>
              )}
              {activeInflection.antonyms?.length > 0 && (
                <>
                  <span className="syn-ant-label">বিপরীত</span>
                  {activeInflection.antonyms.map(a => (
                    <span key={a} className="ant-word" onClick={() => onTagClick?.(a)}>{a}</span>
                  ))}
                </>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
