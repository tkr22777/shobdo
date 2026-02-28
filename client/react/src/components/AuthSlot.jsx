import { useEffect, useRef } from 'react';
import { useAuth } from '../context/AuthContext';

export default function AuthSlot() {
  const { user, signIn, signOut } = useAuth();
  const btnRef = useRef(null);

  useEffect(() => {
    if (user) return;

    const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID;
    const hasRealClientId = clientId && clientId !== 'YOUR_GOOGLE_CLIENT_ID';

    if (!hasRealClientId) {
      // No real Google Client ID configured — show manual prompt button
      if (btnRef.current) {
        btnRef.current.innerHTML = '';
        const btn = document.createElement('button');
        btn.className = 'sign-in-btn';
        btn.textContent = 'সাইন ইন';
        btn.addEventListener('click', () => {
          window.google?.accounts?.id?.prompt();
        });
        btnRef.current.appendChild(btn);
      }
      return;
    }

    const tryRender = () => {
      if (!window.google?.accounts) return false;
      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: (response) => signIn(response.credential),
        auto_select: true,
      });
      if (btnRef.current) {
        window.google.accounts.id.renderButton(btnRef.current, {
          theme: 'outline',
          size: 'small',
          text: 'signin',
          shape: 'pill',
        });
      }
      return true;
    };

    if (!tryRender()) {
      // GSI script not yet loaded — retry after a short delay
      const timer = setTimeout(tryRender, 400);
      return () => clearTimeout(timer);
    }
  }, [user, signIn]);

  if (user) {
    const initials = user.name
      ? user.name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
      : '?';
    return (
      <div className="auth-slot">
        <div className="avatar" title={user.name || ''}>{initials}</div>
        <span className="auth-name">{user.name || user.email || ''}</span>
        <button className="sign-in-btn" onClick={signOut}>সাইন আউট</button>
      </div>
    );
  }

  return (
    <div className="auth-slot">
      <div ref={btnRef} id="googleBtnContainer"></div>
    </div>
  );
}
