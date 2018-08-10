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
        //alert("You clicked near " + lonlat.lat + " N, " +
        //                          + lonlat.lon + " E");
        document.location.hash = "#" + lonlat.lon + "," + lonlat.lat;
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
var zoom = 8;

var cntrposition = new OpenLayers.LonLat(lon, lat);//.transform( fromProjection, toProjection);
var markers = new OpenLayers.Layer.Markers( "Markers" );
markers.addMarker(new OpenLayers.Marker(cntrposition));

var map = new OpenLayers.Map({
    div: "map",
    layers: [
        new OpenLayers.Layer.WMS( "OpenLayers WMS",
                    "http://vmap0.tiles.osgeo.org/wms/vmap0?", {layers: 'basic'} ),
        markers
    ],
    controls: [
        new OpenLayers.Control.Navigation({
            dragPanOptions: {
                enableKinetic: true
            }
        }),
        click,
        new OpenLayers.Control.Attribution()
    ],
    center: [lon, lat],
    zoom: zoom
});

click.activate();

