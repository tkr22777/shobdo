
//Load the Google Transliteration API
google.load("language", "1");
google.load("elements", "1", { packages: "transliteration" });

function onLoad() {

    var options = {
      sourceLanguage: 'en',
      destinationLanguage: ['bn'],
      shortcutKey: 'ctrl+m',
      transliterationEnabled: true
    };

    // Create an instance on TransliterationControl with the required options.
    var control = new google.elements.transliteration.TransliterationControl(options);

    // Enable transliteration in the textfields with the given ids.
    var ids = [ "wordSearchBox" ];

    control.makeTransliteratable(ids);

    // Show the transliteration control which can be used to toggle between
    // English and Bengali and also choose other destination language.
    // control.showControl('translControl');
}

google.setOnLoadCallback(onLoad);
