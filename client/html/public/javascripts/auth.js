(function () {
    'use strict';

    function renderAvatar(user) {
        const slot = document.getElementById('authSlot');
        if (!slot) return;
        const initials = user.name
            ? user.name.split(' ').map(function (w) { return w[0]; }).join('').slice(0, 2).toUpperCase()
            : '?';
        slot.innerHTML =
            '<div class="avatar" title="' + escHtml(user.name || '') + '">' + escHtml(initials) + '</div>' +
            '<span class="auth-name">' + escHtml(user.name || user.email || '') + '</span>' +
            '<button class="sign-in-btn" id="signOutBtn">সাইন আউট</button>';
        document.getElementById('signOutBtn').addEventListener('click', signOut);
    }

    function renderSignInButton() {
        const slot = document.getElementById('authSlot');
        if (!slot) return;
        if (window.GOOGLE_CLIENT_ID && window.GOOGLE_CLIENT_ID !== 'YOUR_GOOGLE_CLIENT_ID' && window.google && window.google.accounts) {
            slot.innerHTML = '<div id="googleBtnContainer"></div>';
            window.google.accounts.id.renderButton(
                document.getElementById('googleBtnContainer'),
                { theme: 'outline', size: 'small', text: 'signin', shape: 'pill' }
            );
        } else {
            slot.innerHTML = '<button class="sign-in-btn" id="signInManualBtn">সাইন ইন</button>';
            const btn = document.getElementById('signInManualBtn');
            if (btn) {
                btn.addEventListener('click', function () {
                    if (window.google && window.google.accounts) {
                        window.google.accounts.id.prompt();
                    }
                });
            }
        }
    }

    function handleCredentialResponse(response) {
        fetch('/api/v1/auth/google', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idToken: response.credential })
        })
        .then(function (res) { return res.json(); })
        .then(function (user) {
            if (user.error) {
                console.error('Sign-in failed:', user.error);
                return;
            }
            window.shobdoUser = user;
            renderAvatar(user);
            document.dispatchEvent(new CustomEvent('userSignedIn', { detail: user }));
        })
        .catch(function (err) {
            console.error('Auth request failed:', err);
        });
    }

    function signOut() {
        fetch('/api/v1/auth/logout', { method: 'POST' })
        .then(function () {
            window.shobdoUser = null;
            renderSignInButton();
            document.dispatchEvent(new CustomEvent('userSignedOut'));
            if (window.google && window.google.accounts) {
                window.google.accounts.id.disableAutoSelect();
            }
        })
        .catch(function (err) {
            console.error('Sign-out failed:', err);
        });
    }

    function escHtml(str) {
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function initGSI() {
        if (!window.GOOGLE_CLIENT_ID || window.GOOGLE_CLIENT_ID === 'YOUR_GOOGLE_CLIENT_ID') {
            return;
        }
        if (!window.google || !window.google.accounts) {
            return;
        }
        window.google.accounts.id.initialize({
            client_id: window.GOOGLE_CLIENT_ID,
            callback: handleCredentialResponse,
            auto_select: true
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        // Check if already signed in
        fetch('/api/v1/auth/me')
        .then(function (res) {
            if (res.ok) return res.json();
            return null;
        })
        .then(function (user) {
            if (user && user.id) {
                window.shobdoUser = user;
                renderAvatar(user);
                document.dispatchEvent(new CustomEvent('userSignedIn', { detail: user }));
            } else {
                window.shobdoUser = null;
                // Wait a tick for GSI script to load, then render sign-in
                setTimeout(function () {
                    initGSI();
                    renderSignInButton();
                }, 200);
            }
        })
        .catch(function () {
            window.shobdoUser = null;
            setTimeout(function () {
                initGSI();
                renderSignInButton();
            }, 200);
        });
    });

    // Handle GSI script loading after DOMContentLoaded
    window.addEventListener('load', function () {
        if (!window.shobdoUser) {
            initGSI();
        }
    });

}());
