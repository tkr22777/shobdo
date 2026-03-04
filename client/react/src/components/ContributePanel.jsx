import { useState, useEffect } from 'react';
import { submitWordCreation, getMyRequests } from '../api';

const POS_OPTIONS = ['', 'বিশেষ্য', 'বিশেষণ', 'ক্রিয়া', 'অব্যয়', 'সর্বনাম', 'ক্রিয়াবিশেষণ'];

const OP_LABEL = { CREATE: 'নতুন', UPDATE: 'সম্পাদনা', DELETE: 'মুছুন' };
const TARGET_LABEL = { WORD: 'শব্দ', MEANING: 'অর্থ' };

function NewWordForm() {
  const [spelling, setSpelling] = useState('');
  const [meaningText, setMeaningText] = useState('');
  const [pos, setPos] = useState('');
  const [example, setExample] = useState('');
  const [status, setStatus] = useState(null); // null | 'submitting' | 'success' | 'error'
  const [errorMsg, setErrorMsg] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    if (!spelling.trim() || !meaningText.trim()) return;
    setStatus('submitting');
    setErrorMsg('');
    try {
      await submitWordCreation(spelling.trim(), meaningText.trim(), pos || null, example.trim() || null);
      setStatus('success');
      setSpelling('');
      setMeaningText('');
      setPos('');
      setExample('');
    } catch (err) {
      setStatus('error');
      setErrorMsg(err.message);
    }
  }

  return (
    <form className="contribute-form" onSubmit={handleSubmit}>
      <h3 className="contribute-section-title">নতুন শব্দ যোগ করুন</h3>
      <p className="contribute-hint">আপনার জমা দেওয়া তথ্য একজন পর্যালোচকের অনুমোদনের পরে প্রকাশিত হবে।</p>

      <label className="contribute-label">শব্দ <span className="required">*</span></label>
      <input
        className="contribute-input"
        type="text"
        value={spelling}
        onChange={e => setSpelling(e.target.value)}
        placeholder="বাংলায় লিখুন…"
        required
      />

      <label className="contribute-label">অর্থ <span className="required">*</span></label>
      <textarea
        className="contribute-textarea"
        value={meaningText}
        onChange={e => setMeaningText(e.target.value)}
        placeholder="সংক্ষিপ্ত সংজ্ঞা লিখুন…"
        rows={3}
        required
      />

      <label className="contribute-label">পদ (ঐচ্ছিক)</label>
      <select className="contribute-select" value={pos} onChange={e => setPos(e.target.value)}>
        {POS_OPTIONS.map(p => <option key={p} value={p}>{p || '— বাছুন —'}</option>)}
      </select>

      <label className="contribute-label">উদাহরণ বাক্য (ঐচ্ছিক)</label>
      <input
        className="contribute-input"
        type="text"
        value={example}
        onChange={e => setExample(e.target.value)}
        placeholder="শব্দটি ব্যবহার করে একটি বাক্য লিখুন…"
      />

      {status === 'success' && (
        <p className="contribute-success">জমা হয়েছে! পর্যালোচনার জন্য অপেক্ষা করুন।</p>
      )}
      {status === 'error' && (
        <p className="contribute-error">{errorMsg || 'জমা দিতে সমস্যা হয়েছে।'}</p>
      )}

      <button
        className="contribute-submit"
        type="submit"
        disabled={status === 'submitting' || !spelling.trim() || !meaningText.trim()}
      >
        {status === 'submitting' ? 'জমা হচ্ছে…' : 'জমা দিন'}
      </button>
    </form>
  );
}

function MySubmissions() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getMyRequests()
      .then(setRequests)
      .catch(() => setError('জমা দেওয়া তথ্য লোড করা যায়নি'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="my-submissions">
      <h3 className="contribute-section-title">আমার জমা</h3>
      {loading && <p className="contribute-hint">লোড হচ্ছে…</p>}
      {error && <p className="contribute-error">{error}</p>}
      {!loading && !error && requests.length === 0 && (
        <p className="contribute-hint">এখনও কোনো জমা নেই।</p>
      )}
      {requests.length > 0 && (
        <ul className="submissions-list">
          {requests.map(r => (
            <li key={r.id} className="submission-item">
              <span className="submission-tag">{TARGET_LABEL[r.targetType] || r.targetType}</span>
              <span className="submission-op">{OP_LABEL[r.operation] || r.operation}</span>
              {r.requestBody?.spelling && (
                <span className="submission-word">{r.requestBody.spelling}</span>
              )}
              {r.requestBody?.text && (
                <span className="submission-text">{r.requestBody.text}</span>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default function ContributePanel() {
  const [tab, setTab] = useState('new'); // 'new' | 'mine'

  return (
    <div className="contribute-panel">
      <h2 className="contribute-title">অবদান</h2>
      <div className="contribute-tabs">
        <button
          className={`contribute-tab${tab === 'new' ? ' active' : ''}`}
          onClick={() => setTab('new')}
        >
          নতুন শব্দ
        </button>
        <button
          className={`contribute-tab${tab === 'mine' ? ' active' : ''}`}
          onClick={() => setTab('mine')}
        >
          আমার জমা
        </button>
      </div>
      {tab === 'new' ? <NewWordForm /> : <MySubmissions />}
    </div>
  );
}
