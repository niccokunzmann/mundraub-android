/*
    {"1":{"pos":["-0.0535529773902500","-78.7753200531005000"],"count":"4"}}
    {"8":{"pos":["11.1807315000000000","12.8287292000000000"],"properties":{"nid":"77535","tid":"15"}}}
    
    -> 
    {
        "position": {
            "lon": float
            "lat": float
        },
        isCluster: boolean,
        category: string, // if isCluster is false
        count: int // if isCluster is true
    }
 */
function createPlantFromMundraub(mundraub) {
    return {
        "position": getLonLatFromPlant(mundraub),
        "isCluster": mundraub.count != undefined,
        "category": getCategoryId(mundraub),
        "count": mundraub.count,
    };
}
function createPlantFromApp(appPlant) {
    return {
        "position": {
            "lon": appPlant.position.longitude,
            "lat": appPlant.position.latitude,
        },
        "isCluster": false,
        "category": appPlant.category,
    };
}

function getLonLatFromPlant(plant) {
    var lon = plant.pos[1];
    var lat = plant.pos[0];
    if (typeof lon == "string") {
        lon = parseFloat(lon);
    }
    if (typeof lat == "string") {
        lat = parseFloat(lat);
    }
    return {lon:lon, lat:lat};
}

function addPlantMarker(plant, layer) {
    var position = lonLatToMarkerPosition(plant.position);
    var icon = getMarkerIconOfPlant(plant);
    var plantMarker = createMarker(position, function() {
        if (plant.isCluster) {
            return "<b>" + plant.count + "</b>"
        } else {
            return "<b>" + translate(getCategoryId(plant)) + "</b>" + 
                "<br/>" + translate("Distance to marker") + " " + 
                distanceString(plant.position, markerPositionToLonLat(marker.lonlat));
        }
    });
    layer.addMarker(plantMarker);
    if (icon) {
        plantMarker.setUrl(icon.url.src);
    }
    if (plant.isCluster) {
        setIconDescriptionOfMarker(plantMarker, plant.count)
    }
    plantMarker.display(true);
    injectClickLink(plantMarker.icon.imageDiv, plantMarker.click);
}

var popups = [];

function destroyAllPopups() {
    while (popups.length > 0) {
        var popup = popups.pop();
        popup.destroy();
        popup.feature.popup = null;
    }
}

function destroyAllPlantMarkers() {
    while (plants.markers.length > 0) {
        var marker = plants.markers[0];
        plants.removeMarker(marker);
        marker.destroy();
    }
    destroyAllPopups();
}

function createMarker(ll, getPopupContentHTML) {
    // create a marker with a pop-up
    // http://dev.openlayers.org/examples/popupMatrix.html
    var feature = new OpenLayers.Feature(markers, ll); 
    feature.closeBox = true;
    feature.popupClass = OpenLayers.Class(OpenLayers.Popup.FramedCloud, {
            'autoSize': true
        });
    feature.data.popupContentHTML = null;
    feature.data.overflow = "auto";//(overflow) ? "auto" : "hidden";
            
    var marker = feature.createMarker();

    var markerClick = function (evt) {
        if (feature.popup == null) {
            feature.data.popupContentHTML = getPopupContentHTML();
            feature.popup = feature.createPopup(feature.closeBox);
            map.addPopup(feature.popup);
            popups.push(feature.popup);
            feature.popup.show();
            feature.popup.feature = feature;
        } else if (!evt.used){
            feature.popup.toggle();
        }
        evt.used = true;
        OpenLayers.Event.stop(evt);
    };
    marker.events.register("click", feature, markerClick);
    marker.click = markerClick;
    return marker;
}

/*
 * Plants from the app.
 */
function loadOwnPlantsFrom(plants) {
    plants.forEach(function(appPlant) {
        var plant = createPlantFromApp(appPlant);
        addPlantMarker(plant, ownPlants);
    });
    ownPlants.div.className += " ownPlants";
}

