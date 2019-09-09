
/*
 * These are general settings.
 */
var API_PROXY_DEFAULT_PORT = 39768;
var API_HOST = "http://localhost:" + API_PROXY_DEFAULT_PORT;

/*
 * These functions are used to request plants from a server.
 */
var PLANT_MARKER_URL = API_HOST + "/cluster/plant"
var PLANT_MARKER_CATEGORIES = "cat=4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37";

function getPlantsInRange(nw, se, zoom, onSuccess) {
    // example: /plant?bbox=13.083043098449709,50.678268138692154,13.151235580444336,50.685827559768505&zoom=15&cat=4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37'
    var requestUrl = PLANT_MARKER_URL + "?bbox=" + nw.lon + "," + se.lat + "," + se.lon + "," + nw.lat + "&zoom=" + zoom + "&" + PLANT_MARKER_CATEGORIES;
    sendRequest(requestUrl, function (markers) {
        log.log("markers for " + requestUrl, markers);
        onSuccess(markers);
    }, function () {
        log.log("Could not request markers from " + requestUrl +
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
                addPlantMarker(createPlantFromMundraub(plant), plants);
            });
        }
    });
}

function updateQuery() {
    return "?date=" + new Date().toISOString();
}

/*
 * Get translations from the app so we do not have to translate twice.
 */
var APP_TRANSLATIONS_URL = API_HOST + "/translations/app.js"
function getAppTranslations(onSuccess) {
    sendRequest(APP_TRANSLATIONS_URL + updateQuery(), onSuccess);
}

/*
 * These functions request the own plants of the user.
 */
var OWN_PLANTS_URL = API_HOST + "/cluster/plants.json";
var OWN_PLANTS = null;
function loadOwnPlants() {
    sendRequest(OWN_PLANTS_URL + updateQuery(), function(ownPlants) {
        OWN_PLANTS = ownPlants; // debug
        log.log("ownPlants: " + ownPlants.length);
        loadOwnPlantsFrom(ownPlants);
    });
}

