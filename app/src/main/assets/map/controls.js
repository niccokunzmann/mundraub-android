
function stopPropagation(e) {
    //console.log("stopPropagation(" + e + ")");
    var event = e || window.event;
    //console.log(event + "stopPropagation()");
    event.stopPropagation();
}

function blockClickThrough(element) {
    var a = document.createElement("a");
    while (element.firstChild) {
        a.appendChild(element.firstChild);
    }
    element.appendChild(a);
    //console.log("blockClickThrough " + element);
    a.onclick = stopPropagation;
    a.addEventListener("click", stopPropagation);
}

function controlsBlockMapClick() {
    var elements = document.getElementsByClassName("olControlNoSelect");
    //console.log("elements" + elements.length);
    for (var i = 0; i < elements.length; i++) {
        var element = elements[i];
        blockClickThrough(element);
    }
}

