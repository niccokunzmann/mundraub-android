
function printError(error) {
    console.log(error.name + ": in " + error.fileName + " in line " + error.lineNumber + ": " + error.message + "\n" + error.stack);
}

console.log("Loading ...");
try{

/*ol.Control.Click = ol.Class(ol.Control, {
    defaultHandlerOptions: {
        'single': true,
        'double': false,
        'pixelTolerance': 0,
        'stopSingle': false,
        'stopDouble': false
    },

    initialize: function(options) {
        this.handlerOptions = ol.Util.extend(
            {}, this.defaultHandlerOptions
        );
        ol.Control.prototype.initialize.apply(
            this, arguments
        ); 
        this.handler = new ol.Handler.Click(
            this, {
                'click': this.trigger
            }, this.handlerOptions
        );
    }, 

    trigger: function(e) {
        var position = getPositionFromPixel(e.xy);
        //alert("You clicked near " + lonlat.lat + " N, " +
        //                          + lonlat.lon + " E");
        //alert(position);
        setPositionInURL(position.lon, position.lat);
        try {
            marker.destroy();
        } catch (e) {
            printError(e);
        }
        marker = new ol.Marker(position.lonlat);
        markers.addMarker(marker);
    }
});*/

function getPositionFromPixel(xy) {
    var lonlat = map.getLonLatFromPixel(xy);
    var position = new ol.LonLat(lonlat.lon, lonlat.lat).transform( toProjection, fromProjection);
    position.lonlat = lonlat;
    position.xy = xy;
    return position;
}

function setPositionInURL(lon, lat) {
    document.location.hash = "#" + lon + "," + lat;
}

//var click = new ol.Control.Click();

var settingsString = document.location.search;

console.log("settingString=" + settingsString);

var startLocation = settingsString.substr(1, settingsString.length).split(",");
var lat = 21.7679;
var lon = 78.8718;
var zoom = 10;
if (startLocation.length == 2) {
    lon = parseFloat(startLocation[0]);
    lat = parseFloat(startLocation[1]);
    zoom = 16;
}
setPositionInURL(lon, lat);

function lonLatToMarkerPosition(lonLat) {
    // http://openlayers.org/en/latest/apidoc/module-ol_proj.html
    return new ol.proj.transform([lonLat.lon, lonLat.lat], fromProjection, toProjection);
}

// projection from https://wiki.openstreetmap.org/wiki/ol_Simple_Example#Add_Markers
var fromProjection = new ol.proj.Projection("EPSG:4326");   // Transform from WGS 1984
var toProjection   = new ol.proj.Projection("EPSG:900913"); // to Spherical Mercator Projection
var position       = lonLatToMarkerPosition({lon:lon, lat:lat});
//var markers = new ol.Layer.Markers( "Markers" );
//var plants = new ol.Layer.Markers( "Plants" );
//var marker = new ol.Marker(position);

//markers.addMarker(marker);

var layer_earth = new ol.source.OSM(
    "Satellite",
    "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/${z}/${y}/${x}/",
    {numZoomLevels: 17});
layer_earth.attribution = "Source: Esri, DigitalGlobe, GeoEye, Earthstar Geographics, CNES/Airbus DS, USDA, USGS, AeroGRID, IGN, and the GIS User Community"; // from https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/
var layer_osm = new ol.layer.Tile({
    source: new ol.source.OSM("Mapnik", "http://a.tile.openstreetmap.org/${z}/${x}/${y}.png", {numZoomLevels: 19})
});

var unsortedMapLayers = [
    // see https://wiki.openstreetmap.org/wiki/ol_Simple_Example#Extensions
    //    new ol.Layer.OSM(),
    layer_earth,
    layer_osm,
    //    new ol.Layer.WMS( "ol WMS", "http://vmap0.tiles.osgeo.org/wms/vmap0?", {layers: 'basic'} ),
    //    new ol.Layer.OSM("OpenTopoMap", "https://{a|b|c}.tile.opentopomap.org/{z}/{x}/{y}.png", {numZoomLevels: 19}),
    ];
var layers = [layer_osm];

var VISIBLE_LAYER = "visibleLayer";
function showRememberedLayer() {
    var visibleLayerName = getCookie(VISIBLE_LAYER);
    console.log("visibleLayerName: " + visibleLayerName);
    unsortedMapLayers.forEach(function (layer, index) {
        if (layer.name == visibleLayerName) {
            layers.unshift(layer);
        } else {
            layers.push(layer);
        }
    });
}
function rememberWhichLayerIsShown() {
    map.events.register("changelayer", this, function(e){
        var layer = e.layer;
        // from https://gis.stackexchange.com/q/110114
        if (layer.visibility && unsortedMapLayers.includes(layer)) {
            console.log("Change to layer: " + layer.name);
            setCookie(VISIBLE_LAYER, layer.name);
        }
    });
}
//showRememberedLayer();
//layers.push(markers);
//layers.push(plants);

var attribution = new ol.control.Attribution();

var map = new ol.Map({
    div: "map",
    layers: layers,
    controls: ol.control.defaults().extend([
        new ol.control.LayerSwitcher(),
        attribution,
    ]),
/*    controls: [
        new ol.Control.Navigation({
            dragPanOptions: {
                enableKinetic: true
            }
        }),
        click,
//        new ol.Control.Attribution(),
        // from https://gis.stackexchange.com/a/83195
  //      new ol.Control.Navigation(),
  //      new ol.Control.PanPanel(),
//        new ol.Control.ZoomPanel()
    ],*/
    view: new ol.View({
        center: position,
        zoom: zoom
      }),
});

//map.addControls([
    // from https://gis.stackexchange.com/a/83195
//    new ol.control.Zoom(),
//    new ol.control.LayerSwitcher(),
//    new ol.control.Navigation(),
//    attribution,
//    click
//]);

//click.activate();
/*
function setPosition(doNotPrint) {
    try {
        map.setCenter(position, zoom);
    } catch (error) {
        if (!doNotPrint) {
            printError(error);
        }
        throw error;
    }
    updatePlants();
}



try {
    setPosition(true);
} catch(error) {
    // size is sometimes null. https://github.com/ol/ol2/issues/669
    //map.size = {"w": document.body.clientWidth, "h": document.body.clientHeight} // could be a solution
    setTimeout(setPosition, 100);
}
rememberWhichLayerIsShown();*/

//map.events.register("moveend", map, function(e){
    // see https://gis.stackexchange.com/a/26619
//    updatePlants();
//});

} catch(error) {
    printError(error)
    throw error;
}
console.log("Done loading.");
