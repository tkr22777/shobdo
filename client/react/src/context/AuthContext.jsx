import { createContext, useContext, useState, useEffect } from 'react';
import { getMe, signInWithGoogle as apiSignIn, signOut as apiSignOut } from '../api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);

  useEffect(() => {
    getMe()
      .then(u => { if (u && u.id) setUser(u); })
      .catch(() => {});
  }, []);

  async function signIn(idToken) {
    const u = await apiSignIn(idToken);
    if (u && !u.error) setUser(u);
    return u;
  }

  async function signOut() {
    await apiSignOut();
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
