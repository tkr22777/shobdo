function printLove() {

        console.log("I love you so much yo!");
        convertToRidmik();
}

function convertToRidmik(englishString) {

    var parser = new RidmikParser();
    return parser.toBangla(englishString);
}

function testTransliterate() {

    if( google.language != null) {

        google.language.transliterate(["k"],"en","bn", function(result) {

            if (result.error) {
              console.log("There was an errror transliterating!");
            } else {
              console.log("There was no errror during transliterating!");
            }
            console.log(JSON.stringify(result));
        });
    } else {
        if(google == null)
            console.log("google is null");

        if(google.language == null)
            console.log("google.language is null");
    }

}

function containsEnglishCharacters(searchTerm) {

    if( searchTerm.match(/[a-z]/i) )
        return true;
    else
        return false;
}

function wordSearch(element) {

    var searchQueryString = $('#wordSearchBox').val().trim();
    console.log("WordSearch searchQueryString: " + searchQueryString );
    var containsEng = containsEnglishCharacters(searchQueryString);
    console.log("Ridmik Conversion: " + convertToRidmik(searchQueryString))

    if( searchQueryString.length > 0 && !containsEng ) { // event.keyCode == 13 && //keyCode 13 is enter

        var searchRoute = "http://127.0.0.1:32779/api/v1/words/search";
        var searchBody = JSON.stringify( { searchString : searchQueryString } );
        RESTPostCall(searchRoute, searchBody, handleWordSearchResult);
    }
}

function handleTestGetResult(data, status, jqXHR) {
    console.log("The data:" + data);
}

function meaningSearch(textContent) {

    var meaningRoute = "http://127.0.0.1:32779/api/v1/words/postget"
    var meaningBody = JSON.stringify( { spelling : textContent } );
    console.log("Meaning route: "  + meaningRoute);
    console.log("Meaning body: "  + meaningBody);
    RESTPostCall(meaningRoute, meaningBody, handleWordMeaningResult);
}

function RESTPostCall( route, postBodyString, onSuccessFunction) {
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

function RESTGetCall( route, onSuccessFunction) {
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

    console.log("Data:" + data);

    $('#wordList').empty();

    $.each(data, function(i, item) {
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


function clearMeaningHolder() {

}

function setMeaningHolder() {

}

function handleMeaningData(data) {

    console.log(data);

    return data.tempMeaningString;

    /*
    var meanings = data.meanings;

    var i = 0;
    var returnString = "<h4><u>" + data.spelling + "</u>";
    for(var key in meanings) {

        console.log(i + " PartsOfSpeech:" + meanings[key].partsOfSpeech);
        console.log(i + " Meaning:" + meanings[key].meaning);
        console.log(i + " Example:" + meanings[key].exampleSentence);
        returnString = returnString + " " + i + " POS:" + meanings[key].partsOfSpeech
        returnString = returnString + " Meaning: " + meanings[key].meaning
        returnString = returnString + " Example: " + meanings[key].exampleSentence
        returnString = returnString + " \n";
        i = i + 1;
    }
    returnString = returnString + "</h4>";
    console.log(returnString);
    return returnString;
    */
}


function listWordElement( element ) {

    var linkedWordText = document.createElement("a");
    linkedWordText.textContent = element;
    linkedWordText.style.color = "white";
    linkedWordText.onclick = function() { meaningSearch(linkedWordText.textContent); };
    var listItem = document.createElement('li')
    listItem.appendChild(linkedWordText);
    listItem.style.color = "white";
    return listItem;
}

function getCount () {

    console.log("In get count!");
    jQuery.ajax({
        type: "GET",
        url: "http://localhost:9000/count",
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function (data, status, jqXHR) {
            console.log("success: " + data);
        },
        error: function (jqXHR, status) {
            console.log("Get count fail");
        }
    });
}

function logResult(data, status, jqXHR){
    console.log("Logging Result:");
    console.log(data);
}

function postTest() {

    console.log("Post test!");
    testRoute = "http://localhost:9000/posttest";
    testBody = JSON.stringify( { name : "SIN" } );
    RESTPostCall(testRoute, testBody,logResult)
}
