function sendRequest(url, onSuccess, onError){
    // see https://developer.mozilla.org/en-US/docs/Learn/HTML/Forms/Sending_forms_through_JavaScript
    var XHR = new XMLHttpRequest();
    if (!onError) {
        onError = function() {
            console.log("ERROR: " + url + " failed.");
        }
    }
    // Define what happens on successful data submission
    XHR.addEventListener('load', function(event) {
        if (event.target.status == 200) {
            if (onSuccess) {
                try {
                    var json = JSON.parse(XHR.responseText);
                } catch (e) {
                    onError(event, e);
                    return;
                }
                onSuccess(json, event);
            }
        } else {
            onError(event);
        }
    });

    // Define what happens in case of error
    XHR.addEventListener('error', function(event) {
        onError(event);
    });

    // Set up our request
    XHR.open('GET', url, true);
    XHR.send(null);
}

var API_PROXY_DEFAULT_PORT = 39768;
var API_HOST = "http://localhost:" + API_PROXY_DEFAULT_PORT;
var PLANT_MARKER_URL = API_HOST + "/cluster/plant"
var PLANT_MARKER_CATEGORIES = "cat=4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37";

function getPlantsInRange(nw, se, zoom, onSuccess) {
    // example: /plant?bbox=13.083043098449709,50.678268138692154,13.151235580444336,50.685827559768505&zoom=15&cat=4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37'
    var requestUrl = PLANT_MARKER_URL + "?bbox=" + nw.lon + "," + se.lat + "," + se.lon + "," + nw.lat + "&zoom=" + zoom + "&" + PLANT_MARKER_CATEGORIES;
    sendRequest(requestUrl, function (markers) {
        console.log("markers for " + requestUrl, markers);
        onSuccess(markers);
    }, function () {
        console.log("Could not request markers from " + requestUrl +
                    " Did you start the server?" + 
                    " https://github.com/niccokunzmann/mundraub-android/blob/master/docs/setup.md#proxy");
    });
}

function getPlantsOnMap(map, onSuccess) {
    var zoom = map.zoom > 13 ? 18 : map.zoom; // show plants for many zoom levels
    getPlantsInRange(
            getLonLatFromPixel({x:0, y:0}),
            getLonLatFromPixel({x:map.size.w, y:map.size.h}),
            zoom,
            onSuccess)
}

function updatePlants() {
    getPlantsOnMap(map, function newPlants(newPlants) {
        destroyAllPlantMarkers();
        if (newPlants) {
            newPlants.features.forEach(function(plant) {
                addPlantMarker(plant);
            });
        }
    });
}

var APP_TRANSLATIONS_URL = API_HOST + "/translations/app.js"

/*
 * Get translations from the app so we do not have to translate twice.
 */
function getAppTranslations(onSuccess) {
    sendRequest(APP_TRANSLATIONS_URL, onSuccess);
}

