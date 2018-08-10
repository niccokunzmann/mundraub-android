
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

var startLocation = document.location.hash.substr(1, document.location.hash.length).split(",");
var lat = 21.7679;
var lon = 78.8718;
if (startLocation.length == 2) {
    lon = parseFloat(startLocation[0]);
    lat = parseFloat(startLocation[1]);
}
var zoom = 5;

// projection from https://wiki.openstreetmap.org/wiki/OpenLayers_Simple_Example#Add_Markers
var fromProjection = new OpenLayers.Projection("EPSG:4326");   // Transform from WGS 1984
var toProjection   = new OpenLayers.Projection("EPSG:900913"); // to Spherical Mercator Projection
var position       = new OpenLayers.LonLat(lon, lat).transform( fromProjection, toProjection);
var markers = new OpenLayers.Layer.Markers( "Markers" );
var marker = new OpenLayers.Marker(position);

markers.addMarker(marker);

var map = new OpenLayers.Map({
    div: "map",
    layers: [
    // see https://wiki.openstreetmap.org/wiki/OpenLayers_Simple_Example#Extensions
        new OpenLayers.Layer.OSM(),
    //    new OpenLayers.Layer.WMS( "OpenLayers WMS", "http://vmap0.tiles.osgeo.org/wms/vmap0?", {layers: 'basic'} ),
    //    new OpenLayers.Layer.OSM("OpenTopoMap", "https://{a|b|c}.tile.opentopomap.org/{z}/{x}/{y}.png", {numZoomLevels: 19}),
        markers
    ],
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
map.addControls([
    // from https://gis.stackexchange.com/a/83195
    new OpenLayers.Control.ZoomPanel(),
    click
]);

click.activate();
map.setCenter(position, zoom);