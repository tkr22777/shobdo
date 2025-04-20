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
    // console.log("Data:" + data);
    $('#wordList').empty();
    $.each(data, function (i, item) {
        $('#wordList').append(
            listWordElement(item)
        );
        //$('#' + item).on("click", function(){ console.log("Clicked list item") } );
    });
}

function handleWordMeaningResult(data, status, jqXHR) {
    var meaningHolder = document.getElementById("wordMeaning");
    meaningHolder.innerHTML = handleMeaningData(data);
}

function handleMeaningData(data) {
    const meanings = data.meanings;
    const totalMeanings = Object.keys(meanings).length;

    const meaningSections = Object.entries(meanings).map(([key, meaning], index) => {
        // Create a function to highlight exact and derived word matches
        // the following styling logic is better pre-computed and set in the backend
        const highlightWord = (sentence, word) => {
            if (!sentence) return '';
            // Split sentence into words and preserve whitespace/punctuation
            return sentence.split(/(\s+|[।,!?])/g).map(part => {
                // If the part contains our word (for derived forms)
                if (part.includes(word)) {
                    return ` <span class="highlighted-word"> ${part} </span> `;
                }
                return part;
            }).join('');
        };

        const sections = [
            totalMeanings > 1
                ? `<div class='meaning-number'>${getBengaliDigit(index + 1)}.</div>` : '',
            `<div class="meaning-text"><u>অর্থ:</u> ${meaning.text}</div>`,
            Array.isArray(meaning.synonyms) && meaning.synonyms.length > 0
                ? `<div><u>সমার্থসমূহ:</u> ${meaning.synonyms.join(', ')}</div>` : '',
            Array.isArray(meaning.antonyms) && meaning.antonyms.length > 0
                ? `<div><u>বিপরীতার্থসমূহ:</u> ${meaning.antonyms.join(', ')}</div>` : '',
            meaning.exampleSentence
                ? `<div><u>উদাহরণ বাক্য:</u> ${highlightWord(meaning.exampleSentence, data.spelling)}</div>` : ''
        ].filter(Boolean).join('');

        return `<div class='meaning-section'>${sections}</div>`;
    }).join('');

    // Add a share button to the title section
    return `
        <div class='word-title-container'>
            <div class='word-title'>${data.spelling}</div>
            <button id="meaningShareButton" class="meaning-share-btn" title="শেয়ার করুন" onclick="copyMeaningUrl('${data.spelling}')">
                <span class="glyphicon glyphicon-share"></span>
            </button>
        </div>
        <div class='meanings-container'>${meaningSections}</div>
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
    var linkedWordText = document.createElement("a");
    linkedWordText.textContent = element;
    var listItem = document.createElement('li');

    // Move the click handler to the li element
    listItem.onclick = function () {
        // Remove active class from all links and list items
        document.querySelectorAll('#wordList a').forEach(a => a.classList.remove('active'));
        document.querySelectorAll('#wordList li').forEach(li => li.classList.remove('active'));

        // Add active class to clicked elements
        linkedWordText.classList.add('active');
        listItem.classList.add('active');

        // Scroll word meaning pane to top before loading new content
        document.getElementById('wordMeaning').scrollTop = 0;

        // Load the meaning for the selected word
        meaningSearch(linkedWordText.textContent);
    };

    listItem.appendChild(linkedWordText);
    return listItem;
}

document.addEventListener('DOMContentLoaded', function () {
    // Set a flag to prevent URL updates during initial page load
    window.isInitialLoad = true;

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
            <div class='word-title'>পরিচিতি</div>
            <div class='meanings-container'>
                <div class='meaning-section'>
                    <div class="meaning-text">
                        <p>শব্দ একটি বাংলা অভিধান অ্যাপ্লিকেশন। এটি বাংলা শব্দের অর্থ, প্রয়োগ ও ব্যাকরণগত বৈশিষ্ট্য খুঁজে পেতে সাহায্য করে।</p>
                    </div>
                </div>
            </div>
        `;
        document.getElementById('wordMeaning').innerHTML = aboutContent;

        // Clear URL parameters when showing about page
        if (!window.isInitialLoad) {
            window.history.replaceState({}, '', window.location.pathname);
        }
    });

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

    // Copy to clipboard
    navigator.clipboard.writeText(shareableUrl)
        .then(() => {
            // Show a temporary tooltip or change the button temporarily
            const shareButton = document.getElementById('meaningShareButton');
            const originalHTML = shareButton.innerHTML;
            shareButton.innerHTML = '<span class="glyphicon glyphicon-ok"></span>';
            setTimeout(() => {
                shareButton.innerHTML = originalHTML;
            }, 2000);
        })
        .catch(err => {
            console.error('Failed to copy: ', err);
            // Fallback for older browsers
            const textArea = document.createElement('textarea');
            textArea.value = shareableUrl;
            document.body.appendChild(textArea);
            textArea.select();
            document.execCommand('copy');
            document.body.removeChild(textArea);

            const shareButton = document.getElementById('meaningShareButton');
            const originalHTML = shareButton.innerHTML;
            shareButton.innerHTML = '<span class="glyphicon glyphicon-ok"></span>';
            setTimeout(() => {
                shareButton.innerHTML = originalHTML;
            }, 2000);
        });
}