
var CONFIGURATION_USE_BROWSER_GPS = true;

function getGPSButton() {
    // from https://gis.stackexchange.com/a/165009

    var button = document.createElement('a');
    button.innerText = translate("GPS");
    button.className = 'gps-button';

    var handleGPSButtonClick = function(e) {
        if (CONFIGURATION_USE_BROWSER_GPS) {
            setGPSHandler(setViewToGPSPosition);
        } else {
            app.setToGPSPosition();
            startGPSActivity();
        }
    };
    
    function setViewToGPSPosition(position) {
        var pos = {lon:position.coords.longitude, lat:position.coords.latitude};
        log.log("GPS position:", pos);
        var newCenter = lonLatToMarkerPosition(pos);
        map.setCenter(newCenter);
        setMarkerToPosition(pos);
    }
    
    function setGPSHandler(handler) {
        if (navigator.geolocation) {
            // from https://www.w3schools.com/html/html5_geolocation.asp
            startGPSActivity();
            navigator.geolocation.getCurrentPosition(function(coords) {
                stopGPSActivity();
                handler(coords);
            });
        } else {
            button.className += " gps-disabled";
        }
    }
    
    var GPSActivityTimeout = null;
    function startGPSActivity() {
        stopGPSActivity();
        function on() {
            button.className = 'gps-button gps-active';
            GPSActivityTimeout = setTimeout(off, 500);
        }
        function off() {
            button.className = 'gps-button';
            GPSActivityTimeout = setTimeout(on, 500);
        }
        GPSActivityTimeout = setTimeout(on, 100);
    }
    
    function stopGPSActivity() {
        button.className = 'gps-button';
        if (GPSActivityTimeout != null) {
            clearTimeout(GPSActivityTimeout);
        }
        GPSActivityTimeout = null;
    }

    button.addEventListener('click', handleGPSButtonClick, false);

    var element = document.createElement('div');
    element.className = 'gps-button-div';
    element.appendChild(button);

    document.body.appendChild(element);
    var GPSControl = new OpenLayers.Control({
        element: element
    });
    return GPSControl;
}
