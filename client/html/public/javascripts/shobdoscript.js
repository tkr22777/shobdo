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

const debouncedWordSearch = debounce(function(element) {
    const searchQueryString = $('#wordSearchBox').val().trim();
    // console.log("WordSearch searchQueryString: " + searchQueryString);

    if (searchQueryString.length > 0) {
        const searchRoute = "/api/v1/words/search";
        const searchString = containsEnglishCharacters(searchQueryString) 
            ? convertToRidmik(searchQueryString)
            : searchQueryString;
            
        console.log("WordSearch searchQueryString: " + searchQueryString + " searchString: " + searchString);
        if (!containsEnglishCharacters(searchString)) {
            const searchBody = JSON.stringify({ searchString });
            RESTPostCall(searchRoute, searchBody, handleWordSearchResult);
        }
    }
}, 100);

function meaningSearch(textContent) {
    var meaningRoute = "/api/v1/words/postget"
    var meaningBody = JSON.stringify({ spelling: textContent });
    // console.log("Meaning route: " + meaningRoute);
    // console.log("Meaning body: " + meaningBody);
    RESTPostCall(meaningRoute, meaningBody, handleWordMeaningResult);
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

function RESTGetCall(route, onSuccessFunction) {
    jQuery.ajax({
        type: "GET",
        url: route,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: onSuccessFunction,
        error: function (jqXHR, status) {
            console.log("GET failed!");
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
                ? `<div class='meaning-number'>${getBengaliDigit(index + 1)}.</div>`: '',
            `<div class="meaning-text"><u>অর্থ:</u> ${meaning.text}</div>`,
            Array.isArray(meaning.synonyms) && meaning.synonyms.length > 0
                ? `<div><u>সমার্থসমূহ:</u> ${meaning.synonyms.join(', ')}</div>`: '',
            Array.isArray(meaning.antonyms) && meaning.antonyms.length > 0
                ? `<div><u>বিপরীতার্থসমূহ:</u> ${meaning.antonyms.join(', ')}</div>`: '',
            meaning.exampleSentence
                ? `<div><u>উদাহরণ বাক্য:</u> ${highlightWord(meaning.exampleSentence, data.spelling)}</div>`: ''
        ].filter(Boolean).join('');

        return `<div class='meaning-section'>${sections}</div>`;
    }).join('');

    return `
        <div class='word-title'>${data.spelling}</div>
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
        
        meaningSearch(linkedWordText.textContent); 
    };
    
    listItem.appendChild(linkedWordText);
    return listItem;
}

document.addEventListener('DOMContentLoaded', function() {
    // Handle about link click
    document.getElementById('aboutLink').addEventListener('click', function(e) {
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
    
    document.getElementById('themeToggle').addEventListener('click', function(e) {
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
});