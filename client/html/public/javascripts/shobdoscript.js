function convertToRidmik(englishString) {
    var parser = new RidmikParser();
    return parser.toBangla(englishString);
}

function containsEnglishCharacters(searchTerm) {
    if (searchTerm.match(/[a-z]/i)) {
        return true;
    } else {
        return false;
    }
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

const debouncedWordSearch = debounce(function (element) {
    const searchQueryString = $('#wordSearchBox').val().trim();

    // Clear text when input is empty
    if (searchQueryString.length === 0) {
        $('#transliteratedText').text("").css({
            'opacity': '0',
            'visibility': 'hidden'
        });
        $('#wordList').empty();

        // Clear the word parameter from URL but keep other parameters
        if (!window.isInitialLoad) {
            const url = new URL(window.location.href);
            url.searchParams.delete('word');
            window.history.replaceState({}, '', url.toString());
        }
        return;
    }

    const searchRoute = "/api/v1/words/search";
    const isEnglish = containsEnglishCharacters(searchQueryString);
    const searchString = isEnglish
        ? convertToRidmik(searchQueryString)
        : searchQueryString;

    // console.log("WordSearch searchQueryString: " + searchQueryString + " searchString: " + searchString);

    // Always show transliterated text for English input
    if (isEnglish) {
        // Use opacity instead of show/hide to prevent layout shifts
        $('#transliteratedText').text("অনুসন্ধানকৃত শব্দ: " + searchString).css({
            'opacity': '1',
            'visibility': 'visible'
        });
    } else {
        // Hide without affecting layout
        $('#transliteratedText').text("").css({
            'opacity': '0',
            'visibility': 'hidden'
        });
    }

    // Only search if we have Bengali text (either directly typed or transliterated)
    if (!containsEnglishCharacters(searchString)) {
        const searchBody = JSON.stringify({ searchString });
        RESTPostCall(searchRoute, searchBody, handleWordSearchResult);

        // Update the URL with the search term
        if (!window.isInitialLoad) {
            const url = new URL(window.location.href);
            url.searchParams.set('q', searchQueryString);
            // Remove the word parameter since we're doing a new search
            url.searchParams.delete('word');
            window.history.replaceState({}, '', url.toString());
        }
    }
}, 100);

function meaningSearch(textContent) {
    var meaningRoute = "/api/v1/words/postget"
    var meaningBody = JSON.stringify({ spelling: textContent });
    // console.log("Meaning route: " + meaningRoute);
    // console.log("Meaning body: " + meaningBody);
    RESTPostCall(meaningRoute, meaningBody, handleWordMeaningResult);

    // Update the URL to reflect the currently viewed word
    // Only update if this wasn't triggered from a URL parameter load
    if (!window.isInitialLoad) {
        const shareableUrl = window.getShareableUrl(textContent);
        window.history.replaceState({}, '', shareableUrl);
    }
}

function RESTPostCall(route, postBodyString, onSuccessFunction) {
    jQuery.ajax({
        type: "POST",
        url: route,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        data: postBodyString,
        success: onSuccessFunction,
        error: function (jqXHR, status) {
            console.log("Post failed!");
        }
    });
}

function RESTGetCall(route, onSuccessFunction, onErrorFunction) {
    jQuery.ajax({
        type: "GET",
        url: route,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        async: true, // Explicitly set to async (this is the default anyway)
        success: onSuccessFunction,
        error: onErrorFunction || function (jqXHR, status, error) {
            console.log(`GET failed! ${route}: ${error}`);
        }
    });
}

function handleWordSearchResult(data, status, jqXHR) {
    $('#wordList').empty();
    if (!data || data.length === 0) {
        const query = $('#wordSearchBox').val().trim();
        $('#wordList').append(
            '<li class="no-results"><strong>কোনো ফলাফল নেই</strong>' +
            (query ? '\u201c' + query + '\u201d \u2014 ' : '') +
            'এই শব্দটি অভিধানে পাওয়া যায়নি।</li>'
        );
        return;
    }
    $.each(data, function (i, item) {
        $('#wordList').append(listWordElement(item));
    });
}

function handleWordMeaningResult(data, status, jqXHR) {
    var meaningHolder = document.getElementById("wordMeaning");
    meaningHolder.innerHTML = handleMeaningData(data);
}

// SVG icons used in generated HTML (no Bootstrap dependency)
const SHARE_SVG = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/></svg>';
const CHECK_SVG = '<svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="20 6 9 17 4 12"/></svg>';

function handleMeaningData(data) {
    const meanings = data.meanings;
    const entries = Object.entries(meanings);
    const totalMeanings = entries.length;

    const highlightWord = (sentence, word) => {
        if (!sentence) return '';
        return sentence.split(/(\s+|[।,!?])/g).map(part => {
            if (part.includes(word)) {
                return `<span class="highlighted-word">${part}</span>`;
            }
            return part;
        }).join('');
    };

    // First meaning text for the article deck (italic lead)
    const firstMeaning = entries[0] ? entries[0][1] : null;
    const deckText = firstMeaning ? firstMeaning.text : '';

    // Collect all synonyms for article footer tags
    const allSynonyms = [];

    // Build article body paragraphs
    const bodyParagraphs = entries.map(([key, meaning], index) => {
        const isFirst = index === 0;
        const hasSynonyms = Array.isArray(meaning.synonyms) && meaning.synonyms.length > 0;
        const hasAntonyms = Array.isArray(meaning.antonyms) && meaning.antonyms.length > 0;
        const hasExample = !!meaning.exampleSentence;

        if (hasSynonyms) allSynonyms.push(...meaning.synonyms);

        let paraContent = '';
        if (totalMeanings > 1) {
            paraContent += `<span class="meaning-number">${getBengaliDigit(index + 1)}.</span> `;
        }

        paraContent += meaning.text;

        const defGraf = `<p class="def-graf">${paraContent}</p>`;

        // Relations (synonyms / antonyms) inline after first def
        let relationsHTML = '';
        if (hasSynonyms || hasAntonyms) {
            const parts = [];
            if (hasSynonyms) parts.push(`<em>সমার্থ:</em> ${meaning.synonyms.join(', ')}`);
            if (hasAntonyms) parts.push(`<em>বিপরীত:</em> ${meaning.antonyms.join(', ')}`);
            relationsHTML = `<p class="example-graf" style="font-style:normal">${parts.join(' &nbsp;·&nbsp; ')}</p>`;
        }

        const exampleGraf = hasExample
            ? `<p class="example-graf">${highlightWord(meaning.exampleSentence, data.spelling)}</p>`
            : '';

        // Pull-quote from the first example sentence when there are multiple meanings
        const pullQuote = (isFirst && totalMeanings > 1 && hasExample)
            ? `<div class="pull-quote">&ldquo;${meaning.exampleSentence}&rdquo;</div>`
            : '';

        return defGraf + relationsHTML + exampleGraf + pullQuote;
    }).join('');

    // Synonym tags in article footer
    const uniqueSynonyms = [...new Set(allSynonyms)].slice(0, 8);
    const footerHTML = uniqueSynonyms.length > 0
        ? `<div class="article-footer">
               <div class="footer-label">সম্পর্কিত শব্দ</div>
               <div class="word-tags">${uniqueSynonyms.map(s => `<span class="word-tag">${s}</span>`).join('')}</div>
           </div>`
        : '';

    return `
        <div class="article">
            <h1 class="article-headline">${data.spelling}</h1>
            ${deckText ? `<div class="article-deck">${deckText}</div>` : ''}
            <div class="article-byline">
                <span>বাংলা</span>
                <button id="meaningShareButton" class="meaning-share-btn"
                    title="শেয়ার করুন" onclick="copyMeaningUrl('${data.spelling}')">
                    ${SHARE_SVG} শেয়ার
                </button>
            </div>
            <div class="article-body">${bodyParagraphs}</div>
            ${footerHTML}
        </div>
    `;
}

function getBengaliDigit(digit) {
    var charCodeForBengaliZero = "০".charCodeAt(0);
    // Convert number to string to handle each digit
    const digitString = digit.toString();
    // Convert each digit and join them together
    return digitString
        .split('')
        .map(d => String.fromCharCode(charCodeForBengaliZero + parseInt(d)))
        .join('');
}


function listWordElement(element) {
    var card = document.createElement('li');
    card.className = 'word-card';

    var wordDiv = document.createElement('div');
    wordDiv.className = 'wc-word';
    wordDiv.textContent = element;

    card.onclick = function () {
        // Remove active from all cards
        document.querySelectorAll('#wordList .word-card').forEach(c => c.classList.remove('active'));
        card.classList.add('active');

        // Scroll word meaning pane to top before loading new content
        document.getElementById('wordMeaning').scrollTop = 0;

        meaningSearch(element);
    };

    card.appendChild(wordDiv);
    return card;
}

document.addEventListener('DOMContentLoaded', function () {
    // Set a flag to prevent URL updates during initial page load
    window.isInitialLoad = true;

    // Populate current date in masthead
    const mastDate = document.getElementById('mastDate');
    if (mastDate) {
        mastDate.textContent = new Date().toLocaleDateString('en-GB', {
            day: 'numeric', month: 'long', year: 'numeric'
        });
    }

    // Initialize transliterated text element
    $('#transliteratedText').css({
        'opacity': '0',
        'visibility': 'hidden'
    });

    // Check for parameters in URL
    const urlParams = new URLSearchParams(window.location.search);
    const searchTerm = urlParams.get('q');
    const specificWord = urlParams.get('word');

    // If a specific word is provided, load its meaning directly
    if (specificWord) {
        meaningSearch(specificWord);

        // If there's also a search term, populate the search box and run the search
        if (searchTerm) {
            $('#wordSearchBox').val(searchTerm);
            debouncedWordSearch();
        }
    }
    // Otherwise just handle the search term if present
    else if (searchTerm) {
        $('#wordSearchBox').val(searchTerm);
        debouncedWordSearch();
    }

    // Clear the initial load flag after a short delay
    setTimeout(() => {
        window.isInitialLoad = false;
    }, 1000);

    // Handle about link click
    document.getElementById('aboutLink').addEventListener('click', function (e) {
        e.preventDefault();
        const aboutContent = `
            <div class="about-article">
                <div class="article-kicker">ABOUT</div>
                <h1 class="article-headline">পরিচিতি</h1>
                <div class="article-deck">বাংলা ভাষার শব্দ ও অর্থের সন্ধানে।</div>
                <div class="article-byline"><span>শব্দ টিম · MMXXIV</span></div>
                <div class="article-body">
                    <p class="def-graf dropcap">শব্দ একটি বাংলা অভিধান অ্যাপ্লিকেশন। এটি বাংলা শব্দের অর্থ, প্রয়োগ ও ব্যাকরণগত বৈশিষ্ট্য খুঁজে পেতে সাহায্য করে।</p>
                </div>
            </div>
        `;
        document.getElementById('wordMeaning').innerHTML = aboutContent;

        // Clear URL parameters when showing about page
        if (!window.isInitialLoad) {
            window.history.replaceState({}, '', window.location.pathname);
        }
    });

    // Click on highlighted words or synonym tags to navigate to that word
    document.getElementById('wordMeaning').addEventListener('click', function (e) {
        const el = e.target;
        if (el.classList.contains('highlighted-word') || el.classList.contains('word-tag')) {
            const word = el.textContent.trim();
            if (!word) return;
            $('#wordSearchBox').val(word);
            RESTPostCall('/api/v1/words/search', JSON.stringify({ searchString: word }), handleWordSearchResult);
            meaningSearch(word);
        }
    });

    // Panel resize
    const wordListPanel = document.querySelector('.word-list-panel');
    const resizeHandle = document.querySelector('.panel-resize-handle');
    if (wordListPanel && resizeHandle) {
        let isResizing = false;
        resizeHandle.addEventListener('mousedown', function (e) {
            isResizing = true;
            resizeHandle.classList.add('dragging');
            document.body.style.cursor = 'col-resize';
            document.body.style.userSelect = 'none';
            e.preventDefault();
        });
        document.addEventListener('mousemove', function (e) {
            if (!isResizing) return;
            const newWidth = e.clientX - wordListPanel.getBoundingClientRect().left;
            if (newWidth >= 160 && newWidth <= 520) {
                wordListPanel.style.width = newWidth + 'px';
            }
        });
        document.addEventListener('mouseup', function () {
            if (!isResizing) return;
            isResizing = false;
            resizeHandle.classList.remove('dragging');
            document.body.style.cursor = '';
            document.body.style.userSelect = '';
        });
    }

    // Theme handling
    const themes = ['green', 'dark', 'blue', 'light'];
    let currentThemeIndex = 0;

    // Check for saved theme
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme) {
        document.documentElement.setAttribute('data-theme', savedTheme);
        currentThemeIndex = themes.indexOf(savedTheme);
    }

    document.getElementById('themeToggle').addEventListener('click', function (e) {
        e.preventDefault();
        currentThemeIndex = (currentThemeIndex + 1) % themes.length;
        const newTheme = themes[currentThemeIndex];

        if (newTheme === 'green') {
            document.documentElement.removeAttribute('data-theme');
        } else {
            document.documentElement.setAttribute('data-theme', newTheme);
        }

        localStorage.setItem('theme', newTheme);
    });

    // Function to get shareable URL with current search term
    window.getShareableUrl = function (specificWord) {
        const searchTerm = $('#wordSearchBox').val().trim();
        const url = new URL(window.location.href);

        // Remove existing parameters
        url.search = '';

        // Add search term if present
        if (searchTerm) {
            url.searchParams.set('q', searchTerm);
        }

        // Add specific word if provided
        if (specificWord) {
            url.searchParams.set('word', specificWord);
        }

        return url.toString();
    };
});

// Function to copy meaning URL to clipboard
function copyMeaningUrl(word) {
    const shareableUrl = window.getShareableUrl(word);
    const shareButton = document.getElementById('meaningShareButton');
    const originalHTML = shareButton.innerHTML;

    const showConfirm = () => {
        shareButton.innerHTML = CHECK_SVG + ' কপি হয়েছে';
        setTimeout(() => { shareButton.innerHTML = originalHTML; }, 2000);
    };

    navigator.clipboard.writeText(shareableUrl)
        .then(showConfirm)
        .catch(err => {
            console.error('Failed to copy: ', err);
            // Fallback for older browsers
            const textArea = document.createElement('textarea');
            textArea.value = shareableUrl;
            document.body.appendChild(textArea);
            textArea.select();
            document.execCommand('copy');
            document.body.removeChild(textArea);
            showConfirm();
        });
}