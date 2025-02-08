function convertToRidmik(englishString) {
    var parser = new RidmikParser();
    return parser.toBangla(englishString);
}

function testTransliterate() {

    if (google.language != null) {

        google.language.transliterate(["k"], "en", "bn", function (result) {
            if (result.error) {
                console.log("There was an errror transliterating!");
            } else {
                console.log("There was no errror during transliterating!");
            }
            console.log(JSON.stringify(result));
        });

    } else {
        if (google == null) {
            console.log("google is null");
        }

        if (google.language == null) {
            console.log("google.language is null");
        }
    }
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
    // console.log(data);
    const meanings = data.meanings;
    const totalMeanings = Object.keys(meanings).length;
    
    const meaningSections = Object.entries(meanings).map(([key, meaning], index) => {
        // console.log("Synonyms:", meaning.synonyms, "Type:", typeof meaning.synonyms);
        // console.log("Antonyms:", meaning.antonyms, "Type:", typeof meaning.antonyms);
        
        const sections = [
            totalMeanings > 1 
                ? `<div class='meaning-number'>${getBengaliDigit(index + 1)}.</div>`: '',
            `<div><u>শব্দের অর্থ:</u> ${meaning.text}</div>`,
            Array.isArray(meaning.synonyms) && meaning.synonyms.length > 0
                ? `<div><u>সমার্থকগুলো:</u> ${meaning.synonyms.join(', ')}</div>`: '',
            Array.isArray(meaning.antonyms) && meaning.antonyms.length > 0
                ? `<div><u>বিপরীতার্থগুলো:</u> ${meaning.antonyms.join(', ')}</div>`: '',
            meaning.exampleSentence
                ? `<div><u>উদাহরণ বাক্য:</u> ${meaning.exampleSentence}</div>`: ''
        ].filter(Boolean).join('');

        return `<div class='meaning-section'>${sections}</div>`;
    }).join('');

    // console.log(returnString);
    return `<div class='word-title'>${data.spelling}</div>${meaningSections}`;
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
        
        meaningSearch(linkedWordText.textContent); 
    };
    
    listItem.appendChild(linkedWordText);
    return listItem;
}