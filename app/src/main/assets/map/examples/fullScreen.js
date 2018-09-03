
function printError(error) {
    console.log(error.name + ": in " + error.fileName + " in line " + error.lineNumber + ": " + error.message + "\n" + error.stack);
}

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
        marker = new OpenLayers.Marker(position.lonlat);
        markers.addMarker(marker);
    }
});


function getPositionFromPixel(xy) {
    var lonlat = map.getLonLatFromPixel(xy);
    var position = new OpenLayers.LonLat(lonlat.lon, lonlat.lat).transform( toProjection, fromProjection);
    position.lonlat = lonlat;
    position.xy = xy;
    return position;
}

function setPositionInURL(lon, lat) {
    document.location.hash = "#" + lon + "," + lat;
}

function lonLatToMarkerPosition(lonLat) {
    return new OpenLayers.LonLat(lonLat.lon, lonLat.lat).transform( fromProjection, toProjection);
}

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


var lat = 21.7679;
var lon = 78.8718;
var zoom = 10;

// projection from https://wiki.openstreetmap.org/wiki/OpenLayers_Simple_Example#Add_Markers
var fromProjection;
var toProjection;
var position;
var markers;
var plants;
var marker;
var map;

function onload() {
    console.log("Loading ...");
    try{


        var click = new OpenLayers.Control.Click();

        var settingsString = document.location.search;

        console.log("settingString=" + settingsString);

        var startLocation = settingsString.substr(1, settingsString.length).split(",");
        if (startLocation.length == 2) {
            lon = parseFloat(startLocation[0]);
            lat = parseFloat(startLocation[1]);
            zoom = 16;
        }
        setPositionInURL(lon, lat);



        // projection from https://wiki.openstreetmap.org/wiki/OpenLayers_Simple_Example#Add_Markers
        fromProjection = new OpenLayers.Projection("EPSG:4326");   // Transform from WGS 1984
        toProjection   = new OpenLayers.Projection("EPSG:900913"); // to Spherical Mercator Projection
        position       = lonLatToMarkerPosition({lon:lon, lat:lat});
        markers = new OpenLayers.Layer.Markers( translate("Location") );
        plants = new OpenLayers.Layer.Markers( translate("Plants") );
        marker = new OpenLayers.Marker(position);

        markers.addMarker(marker);

        var layer_earth = new OpenLayers.Layer.OSM(
            translate("Satellite"),
            "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/${z}/${y}/${x}/",
            {numZoomLevels: 17});
        layer_earth.attribution = "Source: Esri, DigitalGlobe, GeoEye, Earthstar Geographics, CNES/Airbus DS, USDA, USGS, AeroGRID, IGN, and the GIS User Community"; // from https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/
        var layer_osm = new OpenLayers.Layer.OSM("Mapnik", "http://a.tile.openstreetmap.org/${z}/${x}/${y}.png", {numZoomLevels: 19});

        var unsortedMapLayers = [
            // see https://wiki.openstreetmap.org/wiki/OpenLayers_Simple_Example#Extensions
            //    new OpenLayers.Layer.OSM(),
            layer_earth,
            layer_osm,
            //    new OpenLayers.Layer.WMS( "OpenLayers WMS", "http://vmap0.tiles.osgeo.org/wms/vmap0?", {layers: 'basic'} ),
            //    new OpenLayers.Layer.OSM("OpenTopoMap", "https://{a|b|c}.tile.opentopomap.org/{z}/{x}/{y}.png", {numZoomLevels: 19}),
            ];
        var layers = [];

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
        showRememberedLayer();
        layers.push(markers);
        layers.push(plants);

        map = new OpenLayers.Map({
            div: "map",
            layers: layers,
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



        try {
            setPosition(true);
        } catch(error) {
            // size is sometimes null. https://github.com/openlayers/ol2/issues/669
            //map.size = {"w": document.body.clientWidth, "h": document.body.clientHeight} // could be a solution
            setTimeout(setPosition, 100);
        }
        rememberWhichLayerIsShown();

        map.events.register("moveend", map, function(e){
            // see https://gis.stackexchange.com/a/26619
            updatePlants();
        });

    } catch(error) {
        printError(error)
        throw error;
    }
    console.log("Done loading.");
}

window.addEventListener("load", onload);

