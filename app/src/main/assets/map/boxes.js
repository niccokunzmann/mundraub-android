
var CREATE_NEW_BOXES = false;
var boxesLayer;

function addBoxesToMap() {
    /* inspired by http://dev.openlayers.org/examples/drag-feature.html
     * and http://dev.openlayers.org/examples/setextent.html
     */
    // allow testing of specific renderers via "?renderer=Canvas", etc
    var renderer = OpenLayers.Util.getParameters(window.location.href).renderer;
    renderer = (renderer) ? [renderer] : OpenLayers.Layer.Vector.prototype.renderers;
    boxesLayer = new OpenLayers.Layer.Boxes(translate("Offline Map Areas"));
    //new OpenLayers.Layer.Vector(translate("Offline Map Areas"), {
    //    renderers: renderer
    //});
//    var mouseControl = new OpenLayers.Control.MousePosition();
    //map.addControl(mouseControl);
//    var dragControl = new OpenLayers.Control.DragFeature(boxesLayer);
//    map.addControl(dragControl);
//    dragControl.activate();
//    mouseControl.activate();

    map.addLayer(boxesLayer);
    boxesLayer.setVisibility(CREATE_NEW_BOXES);
}

var lastClickLonLat = null;
var boundingBoxes = [];

function notifyBoxesAboutClick(lonlat2) {
    if (!CREATE_NEW_BOXES) {
        return;
    }
    if (lastClickLonLat == null) {
        lastClickLonLat = lonlat2;
        return;
    }
    var lonlat1 = lastClickLonLat;
    lastClickLonLat = null;
    
    var lon1 = Math.min(lonlat1.lon, lonlat2.lon);
    var lon2 = Math.max(lonlat1.lon, lonlat2.lon);
    if (Math.abs(lon1 - lon2) > 180) {
        var tmp = lon1;
        lon1 = lon2;
        lon2 = tmp;
    }
    
    var lat1 = Math.min(lonlat1.lat, lonlat2.lat);
    var lat2 = Math.max(lonlat1.lat, lonlat2.lat);
        
    var bbox = {
        north: lat2,
        south: lat1,
        west: lon1,
        east: lon2,
    };
    addBoundingBox(bbox);
}

function addBoundingBox(bbox) {
    console.log("add bbox", bbox);
    if (boundingBoxes.indexOf(bbox) == -1) {
        boundingBoxes.push(bbox);
    }
    
    if (Math.abs(bbox.west - bbox.east) > 180) {
        bbox.east += 360;
    }

    var point1 = lonLatToMarkerPosition({lon: bbox.west, lat: bbox.south});
    var point2 = lonLatToMarkerPosition({lon: bbox.east, lat: bbox.north});
    var bounds = new OpenLayers.Bounds(point1.lon, point1.lat, point2.lon, point2.lat);

    var box = new OpenLayers.Marker.Box(bounds, "#cc1111", 3); // http://dev.openlayers.org/docs/files/OpenLayers/Marker/Box-js.html
    boxesLayer.addMarker(box);
    box.events.register("click", box, function (e) { // from http://dev.openlayers.org/examples/boxes.html
        if (lastClickLonLat == null) {
            // remove marker
            boxesLayer.removeMarker(this);
            this.destroy();
            boundingBoxes.splice(boundingBoxes.indexOf(bbox), 1);
            setConfigurationInURL();
        } else {
            // create marker with overlap
            var lonlat = getLonLatFromPixel({x: e.clientX, y: e.clientY});
            notifyBoxesAboutClick(lonlat);
        }
    });
    setConfigurationInURL();
    box.div.style.background = "rgba(1, 1, 1, 0.1)";
}

function redrawAllBoundingBoxes() {
    while (boxesLayer.markers.length > 0) {
        var marker = boxesLayer.markers[0];
        marker.destroy();
        boxesLayer.removeMarker(marker);
    }
    boundingBoxes.forEach(addBoundingBox);
}


