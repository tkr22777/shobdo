import { useState, useEffect } from 'react';
import { submitWordCreation, getMyRequests } from '../api';

const OP_LABEL = { CREATE: 'নতুন', UPDATE: 'সম্পাদনা', DELETE: 'মুছুন' };
const TARGET_LABEL = { WORD: 'শব্দ', MEANING: 'অর্থ' };

function NewWordForm() {
  const [spelling, setSpelling] = useState('');
  const [status, setStatus] = useState(null); // null | 'submitting' | 'success' | 'error'
  const [errorMsg, setErrorMsg] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    if (!spelling.trim()) return;
    setStatus('submitting');
    setErrorMsg('');
    try {
      await submitWordCreation(spelling.trim());
      setStatus('success');
      setSpelling('');
    } catch (err) {
      setStatus('error');
      setErrorMsg(err.message);
    }
  }

  return (
    <form className="contribute-form" onSubmit={handleSubmit}>
      <h3 className="contribute-section-title">নতুন শব্দ যোগ করুন</h3>
      <p className="contribute-hint">
        শুধু শব্দের বানান জমা দিন। একজন পর্যালোচক অনুমোদন করলে শব্দটি তৈরি হবে।
        এরপর সেই শব্দের পাতায় গিয়ে অর্থ যোগ করার পরামর্শ দিতে পারবেন।
      </p>

      <label className="contribute-label">শব্দ <span className="required">*</span></label>
      <input
        className="contribute-input"
        type="text"
        value={spelling}
        onChange={e => setSpelling(e.target.value)}
        placeholder="বাংলায় লিখুন…"
        required
        autoFocus
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
        disabled={status === 'submitting' || !spelling.trim()}
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
