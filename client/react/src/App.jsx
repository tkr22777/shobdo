import { useState, useEffect, useRef, useCallback } from 'react';
import { Helmet } from 'react-helmet-async';
import { searchWords, getWordDetail } from './api';
import { RidmikParser } from './lib/ridmik';
import { useLocalStorage } from './hooks/useLocalStorage';
import { useAuth } from './context/AuthContext';
import Masthead from './components/Masthead';
import SearchBar from './components/SearchBar';
import WordList from './components/WordList';
import WordDetail from './components/WordDetail';
import AdminPanel from './components/AdminPanel';
import ContributePanel from './components/ContributePanel';
import ReviewPanel from './components/ReviewPanel';

// Parse /bn/word/ধরা  or  /bn-en/word/ধরা  or legacy ?word=ধরা
function parseCurrentUrl() {
  const { pathname, search } = window.location;
  const params = new URLSearchParams(search);
  const match = pathname.match(/^\/([a-z]{2}(?:-[a-z]{2})?)\/word\/(.+)$/);
  if (match) {
    const [, langSegment, encodedSpelling] = match;
    const [lang, targetLang = null] = langSegment.split('-');
    return { lang, targetLang, spelling: decodeURIComponent(encodedSpelling), query: params.get('q') || '' };
  }
  const legacyWord = params.get('word');
  if (legacyWord) return { lang: 'bn', targetLang: null, spelling: legacyWord, query: params.get('q') || '' };
  return { lang: 'bn', targetLang: null, spelling: null, query: params.get('q') || '' };
}

function buildWordUrl(spelling, lang = 'bn', targetLang = null, query = '') {
  const seg = targetLang ? `${lang}-${targetLang}` : lang;
  const url = new URL(window.location.origin);
  url.pathname = `/${seg}/word/${encodeURIComponent(spelling)}`;
  if (query) url.searchParams.set('q', query);
  return url;
}

function debounce(fn, ms) {
  let t;
  return (...args) => {
    clearTimeout(t);
    t = setTimeout(() => fn(...args), ms);
  };
}

export default function App() {
  const { user } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [transliterated, setTransliterated] = useState('');
  const [searchResults, setSearchResults] = useState(null); // null = not yet searched
  const [selectedSpelling, setSelectedSpelling] = useState(null);
  const [wordDetail, setWordDetail] = useState(null);
  const [viewMode, setViewMode] = useState('empty'); // 'empty' | 'word' | 'settings' | 'admin' | 'contribute' | 'review'
  const [panelWidth, setPanelWidth] = useState(260);
  const [theme, setTheme] = useLocalStorage('theme', 'green');

  const searchInputRef = useRef(null);
  const panelRef = useRef(null);
  const handleRef = useRef(null);
  const isInitialLoad = useRef(true);

  // Apply theme to <html>
  useEffect(() => {
    if (theme && theme !== 'green') {
      document.documentElement.setAttribute('data-theme', theme);
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  }, [theme]);

  const handleThemeChange = useCallback((id) => {
    setTheme(id);
  }, [setTheme]);

  // Core search logic — separated from debouncing
  const performSearch = useCallback(async (query) => {
    if (!query || !query.trim()) {
      setSearchResults(null);
      setTransliterated('');
      return;
    }
    const parser = new RidmikParser();
    const hasEnglish = /[a-z]/i.test(query);
    const searchString = hasEnglish ? parser.toBangla(query) : query;

    setTransliterated(hasEnglish ? 'অনুসন্ধানকৃত শব্দ: ' + searchString : '');

    // Only hit the API if the resolved string is Bengali (no remaining English)
    if (!/[a-z]/i.test(searchString)) {
      try {
        const results = await searchWords(searchString);
        setSearchResults(results || []);
      } catch {
        setSearchResults([]);
      }
    }
  }, []);

  const debouncedSearch = useCallback(debounce(performSearch, 120), [performSearch]);

  // URL sync on mount
  useEffect(() => {
    const { spelling, query, lang } = parseCurrentUrl();
    const isLegacy = !!new URLSearchParams(window.location.search).get('word');

    if (query) {
      setSearchQuery(query);
      performSearch(query);
    }
    if (spelling) {
      if (isLegacy) {
        const canonical = buildWordUrl(spelling, lang, null, query);
        window.history.replaceState({}, '', canonical.toString());
      }
      setSelectedSpelling(spelling);
      setViewMode('word');
      getWordDetail(spelling)
        .then(data => setWordDetail(data))
        .catch(() => {});
    }

    setTimeout(() => { isInitialLoad.current = false; }, 600);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // URL sync on state changes (debounced, skip initial load)
  useEffect(() => {
    if (isInitialLoad.current) return;
    const url = selectedSpelling && viewMode === 'word'
      ? buildWordUrl(selectedSpelling, 'bn', null, searchQuery)
      : new URL(`${window.location.origin}/${searchQuery ? '?q=' + encodeURIComponent(searchQuery) : ''}`);
    window.history.replaceState({}, '', url.toString());
  }, [searchQuery, selectedSpelling, viewMode]);

  // Handlers
  const handleSearchChange = useCallback((val) => {
    setSearchQuery(val);
    if (!val.trim()) {
      setSearchResults(null);
      setTransliterated('');
      return;
    }
    debouncedSearch(val);
  }, [debouncedSearch]);

  const handleWordSelect = useCallback((word) => {
    setSelectedSpelling(word.spelling);
    setViewMode('word');
    getWordDetail(word.spelling)
      .then(data => setWordDetail(data))
      .catch(() => {});
  }, []);

  // Used by SearchBar Surprise Me — receives full word object from /random, skips re-fetch
  const handleSurprise = useCallback((wordData) => {
    const spelling = wordData?.spelling;
    if (!spelling) return;
    setSearchQuery(spelling);
    setSelectedSpelling(spelling);
    setViewMode('word');
    performSearch(spelling);
    if (wordData?.meanings) {
      setWordDetail(wordData);
    } else {
      getWordDetail(spelling).then(setWordDetail).catch(() => {});
    }
  }, [performSearch]);

  // Used by WotdStrip WOTD click, WotdStrip Surprise Me
  const handleNavigate = useCallback((spelling) => {
    setSearchQuery(spelling);
    setSelectedSpelling(spelling);
    setViewMode('word');
    performSearch(spelling);
    getWordDetail(spelling)
      .then(data => setWordDetail(data))
      .catch(() => {});
  }, [performSearch]);

  // Clicking highlighted words or synonym tags in the detail view
  const handleTagClick = useCallback((word) => {
    if (!word) return;
    setSearchQuery(word);
    setSelectedSpelling(word);
    setViewMode('word');
    performSearch(word);
    getWordDetail(word)
      .then(data => setWordDetail(data))
      .catch(() => {});
  }, [performSearch]);

  const handleShare = useCallback(() => {
    const url = selectedSpelling && viewMode === 'word'
      ? buildWordUrl(selectedSpelling, 'bn', null, searchQuery)
      : new URL(window.location.origin);
    navigator.clipboard.writeText(url.toString()).catch(() => {});
  }, [searchQuery, selectedSpelling, viewMode]);

  const handleSettings = useCallback((e) => {
    e.preventDefault();
    setViewMode('settings');
    setWordDetail(null);
    setSelectedSpelling(null);
    if (!isInitialLoad.current) {
      window.history.replaceState({}, '', '/');
    }
  }, []);

  const handleAdmin = useCallback((e) => {
    e.preventDefault();
    setViewMode('admin');
    setWordDetail(null);
    setSelectedSpelling(null);
    if (!isInitialLoad.current) {
      window.history.replaceState({}, '', '/');
    }
  }, []);

  const handleContribute = useCallback((e) => {
    e.preventDefault();
    setViewMode('contribute');
    setWordDetail(null);
    setSelectedSpelling(null);
    if (!isInitialLoad.current) {
      window.history.replaceState({}, '', '/');
    }
  }, []);

  const handleReview = useCallback((e) => {
    e.preventDefault();
    setViewMode('review');
    setWordDetail(null);
    setSelectedSpelling(null);
    if (!isInitialLoad.current) {
      window.history.replaceState({}, '', '/');
    }
  }, []);

  // Keyboard shortcuts
  useEffect(() => {
    const handler = (e) => {
      const tag = (e.target?.tagName || '').toLowerCase();
      const inInput = tag === 'input' || tag === 'textarea';

      if (e.key === '/' && !inInput) {
        e.preventDefault();
        searchInputRef.current?.focus();
        return;
      }
      if (e.key === 'Escape' && inInput) {
        searchInputRef.current?.blur();
        return;
      }
      if (e.key === 'ArrowDown' && inInput) {
        e.preventDefault();
        const cards = document.querySelectorAll('.word-card');
        if (cards.length) {
          searchInputRef.current?.blur();
          cards[0].click();
        }
        return;
      }
      if (e.key === 'ArrowDown' && !inInput) {
        e.preventDefault();
        const cards = Array.from(document.querySelectorAll('.word-card'));
        if (!cards.length) return;
        const active = document.querySelector('.word-card.active');
        const idx = active ? cards.indexOf(active) : -1;
        const next = cards[Math.min(idx + 1, cards.length - 1)];
        next?.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
        next?.click();
        return;
      }
      if (e.key === 'ArrowUp' && !inInput) {
        e.preventDefault();
        const cards = Array.from(document.querySelectorAll('.word-card'));
        if (!cards.length) return;
        const active = document.querySelector('.word-card.active');
        const idx = active ? cards.indexOf(active) : cards.length;
        const prev = cards[Math.max(idx - 1, 0)];
        prev?.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
        prev?.click();
      }
    };
    document.addEventListener('keydown', handler);
    return () => document.removeEventListener('keydown', handler);
  }, []);

  // Panel resize
  useEffect(() => {
    const handle = handleRef.current;
    if (!handle) return;

    let resizing = false;

    const onDown = (e) => {
      resizing = true;
      handle.classList.add('dragging');
      document.body.style.cursor = 'col-resize';
      document.body.style.userSelect = 'none';
      e.preventDefault();
    };
    const onMove = (e) => {
      if (!resizing || !panelRef.current) return;
      const newWidth = e.clientX - panelRef.current.getBoundingClientRect().left;
      if (newWidth >= 160 && newWidth <= 520) setPanelWidth(newWidth);
    };
    const onUp = () => {
      if (!resizing) return;
      resizing = false;
      handle.classList.remove('dragging');
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    };

    handle.addEventListener('mousedown', onDown);
    document.addEventListener('mousemove', onMove);
    document.addEventListener('mouseup', onUp);
    return () => {
      handle.removeEventListener('mousedown', onDown);
      document.removeEventListener('mousemove', onMove);
      document.removeEventListener('mouseup', onUp);
    };
  }, []);

  const isWordView = viewMode === 'word' && wordDetail;
  const firstMeaningText = isWordView
    ? Object.values(wordDetail.meanings || {})[0]?.text || ''
    : '';
  const pageTitle = isWordView
    ? `${wordDetail.spelling} - অর্থ ও সংজ্ঞা | শব্দ`
    : 'শব্দ - বাংলা শব্দকোষ এবং অভিধান | Shobdo - Bengali Dictionary';
  const pageDesc = isWordView
    ? `${wordDetail.spelling} এর অর্থ: ${firstMeaningText}`.slice(0, 160)
    : 'শব্দ - সহজ ও দ্রুত বাংলা শব্দকোষ অনলাইন অভিধান। বাংলা শব্দ খুঁজুন, অর্থ জানুন এবং ব্যবহার শিখুন।';
  const pageUrl = isWordView
    ? `https://www.shobdo.info/bn/word/${encodeURIComponent(wordDetail.spelling)}`
    : 'https://www.shobdo.info/';

  return (
    <>
      <Helmet>
        <title>{pageTitle}</title>
        <meta name="description" content={pageDesc} />
        <meta property="og:title" content={isWordView ? `${wordDetail.spelling} - বাংলা অভিধান` : 'শব্দ - বাংলা শব্দকোষ এবং অভিধান'} />
        <meta property="og:description" content={pageDesc} />
        <meta property="og:url" content={pageUrl} />
        <link rel="canonical" href={pageUrl} />
      </Helmet>
      <Masthead onNavigate={handleNavigate} theme={theme} onThemeChange={handleThemeChange} />
      <SearchBar
        value={searchQuery}
        transliterated={transliterated}
        onChange={handleSearchChange}
        onSurprise={handleSurprise}
        onShare={handleShare}
        inputRef={searchInputRef}
      />
      <main className="main-layout">
        <aside
          className="word-list-panel"
          ref={panelRef}
          style={{ width: panelWidth }}
        >
          <div className="list-header">শব্দ তালিকা</div>
          <nav aria-label="শব্দের তালিকা">
            <WordList
              results={searchResults}
              query={searchQuery}
              selectedSpelling={selectedSpelling}
              onSelect={handleWordSelect}
            />
          </nav>
          <div className="panel-resize-handle" ref={handleRef} title="Drag to resize"></div>
        </aside>
        {viewMode === 'admin' ? (
          <AdminPanel />
        ) : viewMode === 'contribute' ? (
          <ContributePanel />
        ) : viewMode === 'review' ? (
          <ReviewPanel />
        ) : (
          <WordDetail
            data={wordDetail}
            viewMode={viewMode}
            onTagClick={handleTagClick}
          />
        )}
      </main>
      <footer className="site-footer">
        <div className="footer-inner">
          <span className="footer-copy">© ২০২৫ শব্দ</span>
          <div className="footer-theme-swatches">
            {[{ id: 'green', label: 'সবুজ', bg: '#FBF8F0', ring: '#C4881A' },
              { id: 'dark',  label: 'রাত',  bg: '#1A1510', ring: '#D4A83A' }].map(t => (
              <button
                key={t.id}
                className={`footer-swatch${theme === t.id ? ' active' : ''}`}
                style={{ background: t.bg, '--swatch-ring': t.ring }}
                title={t.label}
                onClick={() => handleThemeChange(t.id)}
                aria-label={`থিম: ${t.label}`}
              />
            ))}
          </div>
          <div className="footer-links">
            <a href="#" onClick={handleSettings}>সেটিংস</a>
            {user && (
              <>
                <span>·</span>
                <a href="#" onClick={handleContribute}>অবদান</a>
              </>
            )}
            {(user?.role === 'REVIEWER' || user?.role === 'ADMIN' || user?.role === 'OWNER') && (
              <>
                <span>·</span>
                <a href="#" onClick={handleReview}>পর্যালোচনা</a>
              </>
            )}
            {(user?.role === 'ADMIN' || user?.role === 'OWNER') && (
              <>
                <span>·</span>
                <a href="#" onClick={handleAdmin}>অ্যাডমিন</a>
              </>
            )}
          </div>
        </div>
      </footer>
    </>
  );
}
