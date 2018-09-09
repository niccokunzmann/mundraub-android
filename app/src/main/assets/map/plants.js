
function addPlantMarker(plant) {
    var position = lonLatToMarkerPosition({lon:plant.pos[1], lat:plant.pos[0]});
    var icon = getMarkerIconOfPlant(plant);
    var isPlantCluster = plant.count != undefined;
    var marker = addMarker(position, function() {
        if (isPlantCluster) {
            return "<b>" + plant.count + "</b>"
        } else {
            return "<b>" + translate(tidToName(plant.properties.tid)) + "</b>";
        }
    });
    if (icon) {
        marker.setUrl(icon.url.src);
    }
    if (isPlantCluster) {
        setIconDescriptionOfMarker(marker, plant.count)
    }
    marker.display(true);
    injectClickLink(marker.icon.imageDiv, marker.click);
}

var popups = [];

function destroyAllPopups() {
    while (popups.length > 0) {
        popups.pop().destroy();
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

function addMarker(ll, getPopupContentHTML) {
    // add a marker with a pop-up
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
        } else if (!evt.used){
            feature.popup.toggle();
        }
        evt.used = true;
        OpenLayers.Event.stop(evt);
    };
    marker.events.register("click", feature, markerClick);
    marker.click = markerClick;

    plants.addMarker(marker);
    return marker;
}

