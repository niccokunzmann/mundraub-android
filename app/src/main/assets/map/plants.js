
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
        if (this.popup == null) {
            this.data.popupContentHTML = getPopupContentHTML();
            this.popup = this.createPopup(this.closeBox);
            map.addPopup(this.popup);
            this.popup.show();
        } else {
            this.popup.toggle();
        }
        currentPopup = this.popup;
        OpenLayers.Event.stop(evt);
    };
    marker.events.register("mousedown", feature, markerClick);

    plants.addMarker(marker);
    return marker;
}

