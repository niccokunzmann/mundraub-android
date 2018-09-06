
function blockMapClick(elementId) {
    var element = document.getElementById(elementId);
    element.onclick = function(event) {
        event.stopPropagation();
    }
}

function controlsBlockMapClick() {
    blockMapClick("OpenLayers_Control_Zoom_322");
    blockMapClick("OpenLayers_Control_LayerSwitcher_323");
}

