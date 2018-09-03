
var localizedTranslations = {};
var translationBase = {};

function loadTranslationLanguage(language) {
    script = document.createElement("script");
    script.src = "../translations/" + language + ".js";
    document.head.appendChild(script);
}

var language = navigator.language.split("-")[0];
var baseLanguage = "en";

var define = function (newTranslations) {
    console.log("loaded translations for " + baseLanguage);
    translationBase = newTranslations;
    //console.log("base translations", newTranslations);
    if (baseLanguage != language) {
        define = function(newTranslations) {
            localizedTranslations = newTranslations;
            //console.log(language + " translations", newTranslations);
            console.log("loaded translations for " + language);
        }
        loadTranslationLanguage(language);
    }
}

loadTranslationLanguage(baseLanguage);

function translate(key) {
    var t = localizedTranslations[key];
    if (t != undefined) {
        return t;
    }
    console.log("key " + key + " is not translated to " + language);
    t = translationBase[key];
    if (t != undefined) {
        return t;
    }
    console.log("ERROR: key " + key + " is not found in the translations.");
    return key;
}

