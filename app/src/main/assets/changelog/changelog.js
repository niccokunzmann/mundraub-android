/* To check if this file is working, you need to serve it using an http server.
 * See https://stackoverflow.com/a/6232366/1320237
 */

function setCookie(name,value,days) {
    // from https://stackoverflow.com/a/24103596/1320237
    var expires = "";
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days*24*60*60*1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "")  + expires + "; path=/";
}
function getCookie(name) {
    // from https://stackoverflow.com/a/24103596/1320237
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}

function getAllCommits() {
    var elements = document.getElementsByClassName("change");
    var hashes = [];
    for (var i = 0; i < elements.length; i++) {
        var element = elements[i];
        var hash = element.id;
        hashes.push(hash);
    }
    return hashes;
}

function computeVersionHashes() {
    var thisVersion = {
        "id": currentHash,
        "hashes": getAllCommits().join(",")
    };
    var lastSeenVersion = {
        "id": getCookie("lastSeenVersionId") || "",
        "hashes": getCookie("lastSeenVersionHashes") || "",
    };
    var previousVersion = {
        "id": getCookie("previousVersionId") || "",
        "hashes": getCookie("previousVersionHashes") || "",
    };
    if (!lastSeenVersion.id) {
        lastSeenVersion = thisVersion;
    }
    if (thisVersion.id != lastSeenVersion.id) {
        previousVersion = lastSeenVersion;
        lastSeenVersion = thisVersion;
    }
    setCookie("lastSeenVersionId", lastSeenVersion.id);
    setCookie("lastSeenVersionHashes", lastSeenVersion.hashes);
    setCookie("previousVersionId", previousVersion.id);
    setCookie("previousVersionHashes", previousVersion.hashes);
    return {
        "current": lastSeenVersion.hashes ? lastSeenVersion.hashes.split(",") : [],
        "previous": previousVersion.hashes ? previousVersion.hashes.split(",") : [],
    };
}

window.addEventListener("load", function() {
    hashes = computeVersionHashes();
    hashes.current.forEach(function(currentHash) {
        document.getElementById(currentHash).classList.add(
            hashes.previous.includes(currentHash) ? "previous" : "current"
        );
    });
});

