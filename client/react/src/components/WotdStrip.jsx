import { useState, useEffect } from 'react';
import { getWordOfDay } from '../api';

function todayKey() {
  const d = new Date();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `wotd_${d.getFullYear()}-${mm}-${dd}`;
}

function firstMeaningText(data) {
  if (!data?.meanings) return '';
  const keys = Object.keys(data.meanings);
  return keys.length ? (data.meanings[keys[0]].text || '') : '';
}

export default function WotdStrip({ onNavigate }) {
  const [wotd, setWotd] = useState(null);

  useEffect(() => {
    const key = todayKey();
    let cached = null;
    try { cached = JSON.parse(localStorage.getItem(key)); } catch {}

    if (cached?.spelling) {
      setWotd(cached);
      return;
    }

    getWordOfDay()
      .then(data => {
        if (!data?.spelling) return;
        setWotd(data);
        try { localStorage.setItem(key, JSON.stringify(data)); } catch {}
      })
      .catch(() => {});
  }, []);

  const handleWordClick = () => {
    if (wotd?.spelling) onNavigate(wotd.spelling);
  };

  return (
    <div className="wotd-strip">
      <span className="wotd-label">আজকের শব্দ</span>
      <span className="wotd-sep">&middot;</span>
      <span className="wotd-word" onClick={handleWordClick}>
        {wotd?.spelling || ''}
      </span>
      <span className="wotd-sep">&middot;</span>
      <span className="wotd-def">{wotd ? firstMeaningText(wotd) : ''}</span>
    </div>
  );
}
