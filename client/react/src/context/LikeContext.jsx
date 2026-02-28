import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { getLiked, likeWord as apiLike, unlikeWord as apiUnlike, getLikeCount } from '../api';
import { useAuth } from './AuthContext';

const LikeContext = createContext(null);

export function LikeProvider({ children }) {
  const { user } = useAuth();
  const [liked, setLiked] = useState(new Set());
  const [likeCounts, setLikeCounts] = useState(new Map());

  useEffect(() => {
    if (user) {
      getLiked()
        .then(ids => setLiked(new Set(Array.isArray(ids) ? ids : [])))
        .catch(() => {});
    } else {
      setLiked(new Set());
    }
  }, [user]);

  const fetchLikeCount = useCallback(async (wordId) => {
    if (!wordId) return;
    try {
      const count = await getLikeCount(wordId);
      setLikeCounts(prev => new Map(prev).set(wordId, count));
    } catch {}
  }, []);

  const likeWord = useCallback(async (wordId) => {
    try {
      const data = await apiLike(wordId);
      if (data.liked !== false) {
        setLiked(prev => new Set(prev).add(wordId));
        setLikeCounts(prev => {
          const m = new Map(prev);
          m.set(wordId, (m.get(wordId) || 0) + 1);
          return m;
        });
      }
    } catch {}
  }, []);

  const unlikeWord = useCallback(async (wordId) => {
    try {
      const ok = await apiUnlike(wordId);
      if (ok) {
        setLiked(prev => { const s = new Set(prev); s.delete(wordId); return s; });
        setLikeCounts(prev => {
          const m = new Map(prev);
          m.set(wordId, Math.max(0, (m.get(wordId) || 0) - 1));
          return m;
        });
      }
    } catch {}
  }, []);

  return (
    <LikeContext.Provider value={{ liked, likeCounts, likeWord, unlikeWord, fetchLikeCount }}>
      {children}
    </LikeContext.Provider>
  );
}

export function useLikes() {
  return useContext(LikeContext);
}
