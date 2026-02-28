/**
 * shortcuts.js — Keyboard navigation for Shobdo
 *
 * Fully decoupled: depends only on DOM IDs/classes defined in index.html.
 * Never calls functions from shobdoscript.js directly — interactions are
 * driven by simulating clicks on existing DOM elements so the rest of the
 * app doesn't need to know this file exists.
 *
 * Shortcuts
 * ---------
 *  /          Focus the search box (from anywhere)
 *  Escape     Blur the search box (when focused)
 *  ↓          Next word in list  (or jump to list from search box)
 *  ↑          Previous word in list
 */
(function () {
    'use strict';

    var SEARCH_ID  = 'wordSearchBox';
    var LIST_ID    = 'wordList';
    var CARD_CLASS = 'word-card';

    function searchBox()  { return document.getElementById(SEARCH_ID); }
    function allCards()   { return Array.from(document.querySelectorAll('#' + LIST_ID + ' .' + CARD_CLASS)); }
    function activeCard() { return document.querySelector('#' + LIST_ID + ' .' + CARD_CLASS + '.active'); }

    function activateCard(card) {
        if (!card) return;
        card.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
        card.click();
    }

    document.addEventListener('keydown', function (e) {
        var box       = searchBox();
        var tag       = (e.target && e.target.tagName || '').toLowerCase();
        var inInput   = (tag === 'input' || tag === 'textarea');

        // / — focus search from anywhere outside an input
        if (e.key === '/' && !inInput) {
            e.preventDefault();
            box && box.focus();
            return;
        }

        // Escape — blur search box
        if (e.key === 'Escape' && inInput) {
            box && box.blur();
            return;
        }

        // ↓ inside search box — jump to first result
        if (e.key === 'ArrowDown' && inInput) {
            e.preventDefault();
            var cards = allCards();
            if (cards.length) {
                box && box.blur();
                activateCard(cards[0]);
            }
            return;
        }

        // ↓ in list — move to next card
        if (e.key === 'ArrowDown' && !inInput) {
            e.preventDefault();
            var cards = allCards();
            if (!cards.length) return;
            var current = activeCard();
            var idx = current ? cards.indexOf(current) : -1;
            activateCard(cards[Math.min(idx + 1, cards.length - 1)]);
            return;
        }

        // ↑ in list — move to previous card
        if (e.key === 'ArrowUp' && !inInput) {
            e.preventDefault();
            var cards = allCards();
            if (!cards.length) return;
            var current = activeCard();
            var idx = current ? cards.indexOf(current) : cards.length;
            activateCard(cards[Math.max(idx - 1, 0)]);
            return;
        }
    });
}());
