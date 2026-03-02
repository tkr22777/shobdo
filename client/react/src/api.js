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
  const res = await fetch('/api/v1/words/postget', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ spelling }),
  });
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
