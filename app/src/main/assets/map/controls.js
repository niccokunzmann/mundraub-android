
function controlsBlockMapClick() {
    map.controls.forEach(function (control) {
        if (control.div) {
            control.div.onclick = function(event) {
                event.stopPropagation();
            }
        }
    });
}

