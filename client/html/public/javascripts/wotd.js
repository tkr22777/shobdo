/**
 * wotd.js — Word of the Day + Surprise Me
 *
 * Decoupled: only depends on DOM IDs in index.html and the public
 * GET /api/v1/words/random endpoint. Does not call functions from
 * shobdoscript.js directly — word navigation is driven via .click()
 * on existing word-cards, or by dispatching an 'input' event on the
 * search box so shobdoscript.js handles it naturally.
 *
 * Word of the Day: fetches once per calendar day, cached in localStorage
 * under the key 'wotd_<YYYY-MM-DD>' so the same word persists all day.
 */
(function () {
    'use strict';

    var RANDOM_API   = '/api/v1/words/random';
    var WOTD_PREFIX  = 'wotd_';
    var SEARCH_ID    = 'wordSearchBox';
    var WOTD_WORD_ID = 'wotdWord';
    var WOTD_DEF_ID  = 'wotdDef';
    var SURPRISE_ID  = 'surpriseBtn';

    function todayKey() {
        var d = new Date();
        return WOTD_PREFIX + d.getFullYear() + '-' +
               String(d.getMonth() + 1).padStart(2, '0') + '-' +
               String(d.getDate()).padStart(2, '0');
    }

    function firstMeaningText(data) {
        if (!data || !data.meanings) return '';
        var keys = Object.keys(data.meanings);
        if (!keys.length) return '';
        return data.meanings[keys[0]].text || '';
    }

    function renderStrip(data) {
        var wordEl = document.getElementById(WOTD_WORD_ID);
        var defEl  = document.getElementById(WOTD_DEF_ID);
        if (!wordEl || !defEl || !data || !data.spelling) return;
        wordEl.textContent = data.spelling;
        defEl.textContent  = firstMeaningText(data);
    }

    // Navigate to a word: populate search box + fire search + load meaning
    function navigateTo(spelling) {
        if (!spelling) return;
        var box = document.getElementById(SEARCH_ID);
        if (box) {
            box.value = spelling;
            // Fire oninput/onkeyup so shobdoscript picks it up
            box.dispatchEvent(new Event('keyup', { bubbles: true }));
        }
        // Also load meaning directly via the postget endpoint
        jQuery.ajax({
            type: 'POST',
            url: '/api/v1/words/postget',
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            data: JSON.stringify({ spelling: spelling }),
            success: function (data) {
                // Re-use the handler already defined in shobdoscript.js
                if (typeof handleWordMeaningResult === 'function') {
                    handleWordMeaningResult(data);
                }
            }
        });
    }

    function fetchRandom(callback) {
        jQuery.ajax({
            type: 'GET',
            url: RANDOM_API,
            dataType: 'json',
            success: callback,
            error: function () {
                console.log('wotd: could not fetch random word');
            }
        });
    }

    function loadWotd() {
        var key   = todayKey();
        var cache = null;

        try { cache = JSON.parse(localStorage.getItem(key)); } catch (e) {}

        if (cache && cache.spelling) {
            renderStrip(cache);
            return;
        }

        fetchRandom(function (data) {
            if (!data || !data.spelling) return;
            renderStrip(data);
            try { localStorage.setItem(key, JSON.stringify(data)); } catch (e) {}
        });
    }

    function initSurpriseBtn() {
        var btn = document.getElementById(SURPRISE_ID);
        if (!btn) return;
        btn.addEventListener('click', function () {
            fetchRandom(function (data) {
                if (!data || !data.spelling) return;
                navigateTo(data.spelling);
            });
        });
    }

    function initWotdClick() {
        var wordEl = document.getElementById(WOTD_WORD_ID);
        if (!wordEl) return;
        wordEl.addEventListener('click', function () {
            var spelling = wordEl.textContent.trim();
            if (spelling) navigateTo(spelling);
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        loadWotd();
        initSurpriseBtn();
        initWotdClick();
    });
}());
