import { useCallback, useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useLocalStorage } from '../hooks/useLocalStorage';

const THEMES = [
  { id: 'green', label: 'সবুজ' },
  { id: 'dark',  label: 'রাত' },
];

function StatusCheck() {
  const [status, setStatus] = useState('loading');
  const [details, setDetails] = useState(null);
  const [error, setError]   = useState(null);

  const check = useCallback(() => {
    setStatus('loading');
    setDetails(null);
    setError(null);
    fetch('/api/v1/health', { headers: { Accept: 'application/json' } })
      .then(res => res.json().then(data => ({ ok: res.ok, data })))
      .then(({ ok, data }) => {
        setStatus(ok ? 'healthy' : 'unhealthy');
        if (data && typeof data === 'object') setDetails(data);
      })
      .catch(err => {
        setStatus('unhealthy');
        setError(err.message);
      });
  }, []);

  useEffect(() => { check(); }, [check]);

  const statusText =
    status === 'loading'   ? 'যাচাই করা হচ্ছে…' :
    status === 'healthy'   ? 'সচল আছে ✓' :
                             'সচল নেই';

  return (
    <div className="settings-section">
      <div className="settings-section-title">সিস্টেম</div>
      <div className="hc-status-row">
        <span className={`hc-dot ${status}`}></span>
        <span className="hc-status-text">{statusText}</span>
      </div>
      {details && (
        <div className="hc-details-table">
          <table className="hc-table">
            <tbody>
              {Object.entries(details).map(([k, v]) => (
                <tr key={k}><th>{k}</th><td>{String(v)}</td></tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {error && <p className="hc-error">{error}</p>}
      <button className="hc-refresh-btn" onClick={check} style={{ marginTop: '12px' }}>
        পুনরায় যাচাই
      </button>
    </div>
  );
}

export default function SettingsPage() {
  const { user, signOut } = useAuth();
  const [theme, setTheme] = useLocalStorage('theme', 'green');

  const handleTheme = (id) => {
    setTheme(id);
    if (id && id !== 'green') {
      document.documentElement.setAttribute('data-theme', id);
    } else {
      document.documentElement.removeAttribute('data-theme');
    }
  };

  return (
    <div className="settings-page">
      <div className="settings-header">
        <div className="article-kicker">SETTINGS</div>
        <h1 className="article-headline">সেটিংস</h1>
      </div>

      {/* About */}
      <div className="settings-section">
        <div className="settings-section-title">পরিচিতি</div>
        <p className="settings-body-text">
          শব্দ একটি বাংলা অভিধান অ্যাপ্লিকেশন। এটি বাংলা শব্দের অর্থ, প্রয়োগ ও
          ব্যাকরণগত বৈশিষ্ট্য খুঁজে পেতে সাহায্য করে।
        </p>
      </div>

      {/* Appearance */}
      <div className="settings-section">
        <div className="settings-section-title">থিম</div>
        <div className="theme-picker">
          {THEMES.map(t => (
            <button
              key={t.id}
              className={`theme-btn theme-btn--${t.id}${theme === t.id ? ' active' : ''}`}
              onClick={() => handleTheme(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      {/* Status */}
      <StatusCheck />

      {/* Account */}
      <div className="settings-section">
        <div className="settings-section-title">অ্যাকাউন্ট</div>
        {user ? (
          <div className="settings-account-row">
            <span className="settings-account-name">{user.name || user.email}</span>
            <button className="settings-signout-btn" onClick={signOut}>
              সাইন আউট
            </button>
          </div>
        ) : (
          <p className="settings-body-text" style={{ opacity: 0.65 }}>
            সাইন ইন করলে শব্দ যোগ ও সম্পাদনায় অবদান রাখতে পারবেন।
          </p>
        )}
      </div>
    </div>
  );
}
