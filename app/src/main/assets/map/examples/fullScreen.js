
function printError(error) {
    console.log(error.name + ": in " + error.fileName + " in line " + error.lineNumber + ": " + error.message + "\n" + error.stack);
}

console.log("Loading ...");
try{

OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
    defaultHandlerOptions: {
        'single': true,
        'double': false,
        'pixelTolerance': 0,
        'stopSingle': false,
        'stopDouble': false
    },

    initialize: function(options) {
        this.handlerOptions = OpenLayers.Util.extend(
            {}, this.defaultHandlerOptions
        );
        OpenLayers.Control.prototype.initialize.apply(
            this, arguments
        ); 
        this.handler = new OpenLayers.Handler.Click(
            this, {
                'click': this.trigger
            }, this.handlerOptions
        );
    }, 

    trigger: function(e) {
        var lonlat = map.getLonLatFromPixel(e.xy);
        var position       = new OpenLayers.LonLat(lonlat.lon, lonlat.lat).transform( toProjection, fromProjection);
        //alert("You clicked near " + lonlat.lat + " N, " +
        //                          + lonlat.lon + " E");
        //alert(position);
        document.location.hash = "#" + position.lon + "," + position.lat;
        marker.destroy();
        marker = new OpenLayers.Marker(lonlat);
        markers.addMarker(marker);
    }

});

var click = new OpenLayers.Control.Click();

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

// projection from https://wiki.openstreetmap.org/wiki/OpenLayers_Simple_Example#Add_Markers
var fromProjection = new OpenLayers.Projection("EPSG:4326");   // Transform from WGS 1984
var toProjection   = new OpenLayers.Projection("EPSG:900913"); // to Spherical Mercator Projection
var position       = new OpenLayers.LonLat(lon, lat).transform( fromProjection, toProjection);
var markers = new OpenLayers.Layer.Markers( "Markers" );
var marker = new OpenLayers.Marker(position);

markers.addMarker(marker);

var layer_earth = new OpenLayers.Layer.OSM(
    "Satellite",
    "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/${z}/${y}/${x}/",
    {numZoomLevels: 17});
layer_earth.attribution = "Source: Esri, DigitalGlobe, GeoEye, Earthstar Geographics, CNES/Airbus DS, USDA, USGS, AeroGRID, IGN, and the GIS User Community"; // from https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/
var layer_osm = new OpenLayers.Layer.OSM("Mapnik", "http://a.tile.openstreetmap.org/${z}/${x}/${y}.png", {numZoomLevels: 19});

var map = new OpenLayers.Map({
    div: "map",
    layers: [
    // see https://wiki.openstreetmap.org/wiki/OpenLayers_Simple_Example#Extensions
    //    new OpenLayers.Layer.OSM(),
    layer_earth,
    layer_osm,
    //    new OpenLayers.Layer.WMS( "OpenLayers WMS", "http://vmap0.tiles.osgeo.org/wms/vmap0?", {layers: 'basic'} ),
    //    new OpenLayers.Layer.OSM("OpenTopoMap", "https://{a|b|c}.tile.opentopomap.org/{z}/{x}/{y}.png", {numZoomLevels: 19}),
        markers
    ],
    controls: [],
/*    controls: [
        new OpenLayers.Control.Navigation({
            dragPanOptions: {
                enableKinetic: true
            }
        }),
        click,
//        new OpenLayers.Control.Attribution(),
        // from https://gis.stackexchange.com/a/83195
  //      new OpenLayers.Control.Navigation(),
  //      new OpenLayers.Control.PanPanel(),
//        new OpenLayers.Control.ZoomPanel()
    ],*/
});

attribution = new OpenLayers.Control.Attribution();
map.addControls([
    // from https://gis.stackexchange.com/a/83195
    new OpenLayers.Control.Zoom(),
    new OpenLayers.Control.LayerSwitcher(),
    new OpenLayers.Control.Navigation(),
    attribution,
    click
]);

click.activate();

function setPosition() {
    try {
        map.setCenter(position, zoom);
    } catch (error) {
        printError(error);
        throw error;
    }
}

try {
    setPosition();
} catch(error) {
    // size is sometimes null. https://github.com/openlayers/ol2/issues/669
    //map.size = {"w": document.body.clientWidth, "h": document.body.clientHeight} // could be a solution
    setTimeout(setPosition, 100);
}


} catch(error) {
    printError(error)
    throw error;
}
console.log("Done loading.");
