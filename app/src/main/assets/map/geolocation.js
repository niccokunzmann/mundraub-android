

function getGPSButton() {
    // from https://gis.stackexchange.com/a/165009

    var button = document.createElement('a');
    button.innerText = translate("GPS");
    button.className = 'gps-button';

    var handleGPSButtonClick = function(e) {
        setGPSHandler(setViewToGPSPosition);
    };
    
    function setViewToGPSPosition(position) {
        var newCenter = lonLatToMarkerPosition({lon:position.coords.longitude,
                                                lat:position.coords.latitude});
        map.setCenter(newCenter);
    }
    
    function setGPSHandler(handler) {
        if (navigator.geolocation) {
            // from https://www.w3schools.com/html/html5_geolocation.asp
            navigator.geolocation.getCurrentPosition(handler);
        } else {
            button.className += " gps-disabled";
        }
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
