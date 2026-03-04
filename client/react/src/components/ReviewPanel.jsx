import { useState, useEffect } from 'react';
import { getPendingRequests, approveRequest } from '../api';

const OP_LABEL = { CREATE: 'নতুন', UPDATE: 'সম্পাদনা', DELETE: 'মুছুন' };
const TARGET_LABEL = { WORD: 'শব্দ', MEANING: 'অর্থ' };

function RequestCard({ request, onApproved }) {
  const [status, setStatus] = useState(null); // null | 'approving' | 'error'
  const [errorMsg, setErrorMsg] = useState('');

  async function handleApprove() {
    setStatus('approving');
    setErrorMsg('');
    try {
      await approveRequest(request.id);
      onApproved(request.id);
    } catch (err) {
      setStatus('error');
      setErrorMsg(err.message);
    }
  }

  const opLabel = OP_LABEL[request.operation] || request.operation;
  const targetLabel = TARGET_LABEL[request.targetType] || request.targetType;
  const content = request.requestBody?.spelling || request.requestBody?.text || '—';
  const submitter = request.submitterId
    ? request.submitterId.slice(0, 8) + '…'
    : '?';

  return (
    <div className="review-card">
      <div className="review-card-meta">
        <span className="submission-tag">{targetLabel}</span>
        <span className="submission-op">{opLabel}</span>
        <span className="review-submitter" title={request.submitterId}>{submitter}</span>
      </div>
      <div className="review-card-content">{content}</div>
      {status === 'error' && (
        <p className="contribute-error">{errorMsg}</p>
      )}
      <button
        className="review-approve-btn"
        onClick={handleApprove}
        disabled={status === 'approving'}
      >
        {status === 'approving' ? 'অনুমোদন হচ্ছে…' : 'অনুমোদন করুন'}
      </button>
    </div>
  );
}

export default function ReviewPanel() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    getPendingRequests()
      .then(setRequests)
      .catch(() => setError('অনুরোধ লোড করা যায়নি'))
      .finally(() => setLoading(false));
  }, []);

  function handleApproved(requestId) {
    setRequests(prev => prev.filter(r => r.id !== requestId));
  }

  return (
    <div className="contribute-panel">
      <h2 className="contribute-title">পর্যালোচনা</h2>
      {loading && <p className="contribute-hint">লোড হচ্ছে…</p>}
      {error && <p className="contribute-error">{error}</p>}
      {!loading && !error && requests.length === 0 && (
        <p className="contribute-hint">কোনো মুলতুবি অনুরোধ নেই।</p>
      )}
      {requests.length > 0 && (
        <div className="review-list">
          {requests.map(r => (
            <RequestCard key={r.id} request={r} onApproved={handleApproved} />
          ))}
        </div>
      )}
    </div>
  );
}
