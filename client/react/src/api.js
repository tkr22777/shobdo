// All API calls — no jQuery, pure fetch()

export async function searchWords(searchString) {
  const res = await fetch('/api/v1/words/search', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ searchString }),
  });
  if (!res.ok) throw new Error('Search failed');
  return res.json();
}

export async function getWordDetail(spelling) {
  // GET /api/v1/bn/word/:spelling — also falls back to InflectionIndex and returns
  // `inflectedFrom` field when the spelling is an inflected form of another word.
  const res = await fetch('/api/v1/bn/word/' + encodeURIComponent(spelling));
  if (!res.ok) throw new Error('Word detail failed');
  return res.json();
}

export async function getWordOfDay() {
  const res = await fetch('/api/v1/words/daily');
  if (!res.ok) throw new Error('Word of the day failed');
  return res.json();
}

export async function getRandomWord() {
  const res = await fetch('/api/v1/words/random');
  if (!res.ok) throw new Error('Random word failed');
  return res.json();
}

export async function getMe() {
  const res = await fetch('/api/v1/auth/me');
  if (!res.ok) return null;
  return res.json();
}

export async function signInWithGoogle(idToken) {
  const res = await fetch('/api/v1/auth/google', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ idToken }),
  });
  if (!res.ok) throw new Error('Sign-in failed');
  return res.json();
}

export async function signOut() {
  await fetch('/api/v1/auth/logout', { method: 'POST' });
}

export async function getLiked() {
  const res = await fetch('/api/v1/likes');
  if (!res.ok) return [];
  return res.json();
}

export async function likeWord(wordId) {
  const res = await fetch('/api/v1/likes', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ wordId }),
  });
  return res.json();
}

export async function unlikeWord(wordId) {
  const res = await fetch('/api/v1/likes/' + encodeURIComponent(wordId), {
    method: 'DELETE',
  });
  return res.ok;
}

export async function getLikeCount(wordId) {
  const res = await fetch('/api/v1/likes/count/' + encodeURIComponent(wordId));
  if (!res.ok) return 0;
  const data = await res.json();
  return data.count || 0;
}

// Word creation requests accept spelling only.
// Meanings must be submitted as separate requests once the word is approved.
export async function submitWordCreation(spelling) {
  const res = await fetch('/api/v1/requests/words', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ spelling }),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || 'Submission failed');
  }
  return res.json();
}

export async function submitMeaningCreation(wordId, meaningText, partOfSpeech, exampleSentence) {
  const meaning = { text: meaningText };
  if (partOfSpeech) meaning.partOfSpeech = partOfSpeech;
  if (exampleSentence) meaning.exampleSentence = exampleSentence;
  const res = await fetch('/api/v1/requests/words/' + encodeURIComponent(wordId) + '/meanings', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(meaning),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || 'Submission failed');
  }
  return res.json();
}

export async function submitMeaningUpdate(wordId, meaningId, meaningText, partOfSpeech, exampleSentence) {
  const meaning = { id: meaningId, text: meaningText };
  if (partOfSpeech) meaning.partOfSpeech = partOfSpeech;
  if (exampleSentence) meaning.exampleSentence = exampleSentence;
  const res = await fetch('/api/v1/requests/words/' + encodeURIComponent(wordId) + '/meanings', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(meaning),
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || 'Submission failed');
  }
  return res.json();
}

export async function getMyRequests() {
  const res = await fetch('/api/v1/requests/mine');
  if (!res.ok) throw new Error('Failed to load submissions');
  return res.json();
}

export async function getPendingRequests() {
  const res = await fetch('/api/v1/requests');
  if (!res.ok) throw new Error('Failed to load pending requests');
  return res.json();
}

export async function approveRequest(requestId) {
  const res = await fetch('/api/v1/requests/' + encodeURIComponent(requestId) + '/approve', {
    method: 'POST',
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new Error(err.error || 'Approval failed');
  }
  return res.ok;
}

export async function listUsers() {
  const res = await fetch('/api/v1/admin/users');
  if (!res.ok) throw new Error('Failed to list users');
  return res.json();
}

export async function assignRole(userId, role) {
  const res = await fetch('/api/v1/admin/users/' + encodeURIComponent(userId) + '/role', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ role }),
  });
  if (!res.ok) throw new Error('Failed to assign role');
  return res.json();
}
