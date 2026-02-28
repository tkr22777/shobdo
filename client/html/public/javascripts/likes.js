(function () {
    'use strict';

    var liked = new Set(); // set of wordIds the current user has liked

    function fetchLiked() {
        fetch('/api/v1/likes')
        .then(function (res) {
            if (!res.ok) return [];
            return res.json();
        })
        .then(function (wordIds) {
            liked = new Set(Array.isArray(wordIds) ? wordIds : []);
            refreshAllHearts();
        })
        .catch(function (err) {
            console.error('fetchLiked error:', err);
        });
    }

    function refreshAllHearts() {
        document.querySelectorAll('.like-btn[data-word-id]').forEach(function (btn) {
            var wordId = btn.dataset.wordId;
            if (liked.has(wordId)) {
                btn.classList.add('liked');
                btn.textContent = '♥';
            } else {
                btn.classList.remove('liked');
                btn.textContent = '♡';
            }
        });
    }

    function fetchAndUpdateCount(wordId) {
        fetch('/api/v1/likes/count/' + encodeURIComponent(wordId))
        .then(function (res) { return res.json(); })
        .then(function (data) {
            document.querySelectorAll('.like-count[data-word-id="' + wordId + '"]').forEach(function (el) {
                el.textContent = data.count > 0 ? data.count : '';
            });
        })
        .catch(function () {});
    }

    // Watch #wordMeaning for new article nodes (word detail loaded)
    var meaningObserver = new MutationObserver(function () {
        var byline = document.querySelector('#wordMeaning .article-byline[data-word-id]');
        if (!byline) return;
        var wordId = byline.dataset.wordId;
        if (!wordId) return;
        fetchAndUpdateCount(wordId);
        refreshAllHearts();
    });

    // Watch #wordList for new cards
    var listObserver = new MutationObserver(function () {
        refreshAllHearts();
    });

    // Event delegation for like button clicks
    document.addEventListener('click', function (e) {
        var btn = e.target.closest('.like-btn[data-word-id]');
        if (!btn) return;
        var wordId = btn.dataset.wordId;
        if (!wordId) return;

        if (!window.shobdoUser) {
            var orig = btn.textContent;
            btn.textContent = 'সাইন ইন করুন';
            setTimeout(function () { btn.textContent = orig; }, 2000);
            return;
        }

        if (liked.has(wordId)) {
            fetch('/api/v1/likes/' + encodeURIComponent(wordId), { method: 'DELETE' })
            .then(function (res) {
                if (res.ok) {
                    liked.delete(wordId);
                    refreshAllHearts();
                    updateCountDisplay(wordId, -1);
                }
            })
            .catch(function (err) { console.error('unlike error:', err); });
        } else {
            fetch('/api/v1/likes', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ wordId: wordId })
            })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data.liked !== false) {
                    liked.add(wordId);
                    refreshAllHearts();
                    updateCountDisplay(wordId, 1);
                }
            })
            .catch(function (err) { console.error('like error:', err); });
        }
    });

    function updateCountDisplay(wordId, delta) {
        document.querySelectorAll('.like-count[data-word-id="' + wordId + '"]').forEach(function (el) {
            var current = parseInt(el.textContent, 10) || 0;
            var next = current + delta;
            el.textContent = next > 0 ? next : '';
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        var wordMeaning = document.getElementById('wordMeaning');
        var wordList = document.getElementById('wordList');

        if (wordMeaning) {
            meaningObserver.observe(wordMeaning, { childList: true });
        }
        if (wordList) {
            listObserver.observe(wordList, { childList: true });
        }

        document.addEventListener('userSignedIn', function () {
            fetchLiked();
        });

        document.addEventListener('userSignedOut', function () {
            liked = new Set();
            refreshAllHearts();
        });
    });

}());
