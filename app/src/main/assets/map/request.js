function sendRequest(url, onSuccess, onError){
    // see https://developer.mozilla.org/en-US/docs/Learn/HTML/Forms/Sending_forms_through_JavaScript
    var XHR = new XMLHttpRequest();
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
            if (onError) {
                onError(event);
            }
        }
    });

    // Define what happens in case of error
    XHR.addEventListener('error', function(event) {
        if (onError) {
            onError(event);
        }
    });

    // Set up our request
    XHR.open('GET', url, true);
    XHR.send(null);
}

var API_PROXY_DEFAULT_PORT = 39768;
var PLANT_MARKER_URL = "http://localhost:" + API_PROXY_DEFAULT_PORT + "/plant"
var PLANT_MARKER_CATEGORIES = "cat=4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37";

function getPlantsInRange(corner1, corner2, zoom, onSuccess) {
    var minLat = Math.min(corner1.lat, corner2.lat);
    var maxLat = Math.max(corner1.lat, corner2.lat);
    var minLon = Math.min(corner1.lon, corner2.lon);
    var maxLon = Math.max(corner1.lon, corner2.lon);
    // example: /plant?bbox=13.083043098449709,50.678268138692154,13.151235580444336,50.685827559768505&zoom=15&cat=4,5,6,7,8,9,10,11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37'
    var requestUrl = PLANT_MARKER_URL + "?bbox=" + minLon + "," + minLat + "," + maxLon + "," + maxLat + "&zoom=" + zoom + "&" + PLANT_MARKER_CATEGORIES;
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
            map.getLonLatFromPixel({x:0, y:0}),
            map.getLonLatFromPixel({x:map.size.w, y:map.size.h}),
            zoom,
            onSuccess)
}

function updatePlants() {
    getPlantsOnMap(map, function newPlants(newPlants) {
        while (plants.markers.length > 0) {
            var marker = plants.markers[0];
            plants.removeMarker(marker);
            marker.destroy();
        }
        if (newPlants) {
            newPlants.features.forEach(function(plant) {
                var position = lonLatToMarkerPosition({lon:plant.pos[1], lat:plant.pos[0]});
                var icon = getMarkerIconOfPlant(plant);
                var marker = new OpenLayers.Marker(position);
                if (icon) {
                    marker.setUrl(icon.url.src);
                }
                if (plant.count) {
                    setIconDescriptionOfMarker(marker, plant.count)
                }
                plants.addMarker(marker);
                marker.display(true);
            });
        }
    });
}
