
onChangeSetPosition = function() {
    // defer lookup until function is loaded.
    setPosition();
}

function onChangeRedrawAllBoundingBoxes() {
    // defer lookup until function is loaded.
    redrawAllBoundingBoxes();
}

function layerUrlConfiguration(layerId) {
    return {
        "set": function(url) { mapLayersById[layerId].url = url; },
        "get": function(url) { return mapLayersById[layerId].url; },
        "onchange": [function(url) { mapLayersById[layerId].redraw(); }],
    };
}

function updateShowBoxes() {
    if (CREATE_NEW_BOXES) {
        boxesLayer.setVisibility(true);
    }
}

var queryHandlers = {
    "centerLon": {
        "set": function(lon) { center.lon = parseFloat(lon); },
        "get": function() { return center.lon; },
        "onchange": [onChangeSetPosition],
    },
    "centerLat": {
        "set": function(lat) { center.lat = parseFloat(lat); },
        "get": function() { return center.lat; },
        "onchange": [onChangeSetPosition],
    },
    "markerLon": {
        "set": function(lon) {
            var pos = getMarkerPosition();
            pos.lon = parseFloat(lon);
            setMarkerToPosition(pos);
        },
        "get": function() { return getMarkerPosition().lon; }
    },
    "markerLat": {
        "set": function(lat) {
            var pos = getMarkerPosition();
            pos.lat = parseFloat(lat);
            setMarkerToPosition(pos);
        },
        "get": function() { return getMarkerPosition().lat; }
    },
    "zoom": {
        "set": function(zoom_) { zoom = parseInt(zoom_); },
        "get": function() { return map ? map.zoom : zoom; },
        "onchange": [onChangeSetPosition],
    },
    "mapnikUrl": layerUrlConfiguration("osm"),
    "earthUrl": layerUrlConfiguration("earth"),
    "browserGPS": {
        "set": function(use) { CONFIGURATION_USE_BROWSER_GPS = use == "true"; },
        "get": function() { return CONFIGURATION_USE_BROWSER_GPS ? "true" : "false"; },
    },
    "createBoxes": {
        "set": function(use) { CREATE_NEW_BOXES = use == "true"; },
        "get": function() { return CREATE_NEW_BOXES ? "true" : "false"; },
        "onchange": [updateShowBoxes]
    },
    "boxes": {
        "set": function(boxesJSON) { boundingBoxes = JSON.parse(boxesJSON); },
        "get": function() { return JSON.stringify(boundingBoxes); },
        "onchange": [onChangeRedrawAllBoundingBoxes]
    },
};



function getConfigurationFromURL() {
    // from http://stackoverflow.com/a/1099670/1320237
    var qs = document.location.hash.length != 0 ? document.location.hash : document.location.search;
    var qs = qs.substring(1, qs.length);
    var tokens, re = /[?&]?([^=]+)=([^&]*)/g;
    qs = qs.split("+").join(" ");

    var updates = [onChangeSetPosition];
    while (tokens = re.exec(qs)) {
        var id = decodeURIComponent(tokens[1]);
        var content = decodeURIComponent(tokens[2]);
        handler = queryHandlers[id];
        if (handler) {
            var valueBefore = handler.get();
            handler.set(content);
            var valueAfter = handler.get();
            if (valueBefore != valueAfter && handler.onchange) {
                handler.onchange.forEach(function (onchange) {
                    if (!updates.includes(onchange)) {
                        updates.push(onchange);
                    }
                });
            }
        }
    }
    updates.forEach(function (update) {update();});
}

function setConfigurationInURL() {
    var query = [];
    properties(queryHandlers).forEach(function(id) {
        var key = encodeURIComponent(id);
        var value = encodeURIComponent(queryHandlers[id].get());
        query.push(key + "=" + value);
    });
    document.location.hash = query.join("&");
}

function properties(object) {
    var properties = [];
    // from https://stackoverflow.com/a/16735184
    for (var property in object) {
        if (object.hasOwnProperty(property)) {
            properties.push(property);
        }
    }
    return properties;
}

function configurationOnLoad() {
    getConfigurationFromURL();
    var zoomButtons = [
        document.getElementsByClassName("olControlZoomIn")[0],
        document.getElementsByClassName("olControlZoomOut")[0],
    ];
    zoomButtons.forEach(function(button) {
        button.removeAttribute("href"); // delete attribute from http://www.java2s.com/Tutorial/JavaScript/0420__HTML-Tags/RemoveattributebycallingtheremoveAttributefunction.htm
    });
}

