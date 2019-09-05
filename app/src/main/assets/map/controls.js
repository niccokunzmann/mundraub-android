
function stopPropagation(e) {
    //log.log("stopPropagation(" + e + ")");
    var event = e || window.event;
    //log.log(event + "stopPropagation()");
    event.stopPropagation();
}

function injectLinkBeforeChildren(element) {
    var a = document.createElement("a");
    while (element.firstChild) {
        a.appendChild(element.firstChild);
    }
    element.appendChild(a);
    return a;
}

function injectClickLink(element, onClick) {
    // this is a hack for old browsers unable to use div.onclick
    var a = injectLinkBeforeChildren(element);
    a.onclick = onClick;
    a.addEventListener("click", onClick);
    return a;
}

function blockClickThrough(element) {
    injectClickLink(element, stopPropagation);
}

function controlsBlockMapClick() {
    var elements = document.getElementsByClassName("olControlNoSelect");
    //log.log("elements" + elements.length);
    for (var i = 0; i < elements.length; i++) {
        var element = elements[i];
        blockClickThrough(element);
    }
}

