/* To check if this file is working, you need to serve it using an http server.
 * See https://stackoverflow.com/a/6232366/1320237
 */

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
        console.log("The changelog was never visited.");
    }
    if (thisVersion.id != lastSeenVersion.id) {
        previousVersion = lastSeenVersion;
        lastSeenVersion = thisVersion;
        console.log("This version of the changelog was never seen.");
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
    var hashes = computeVersionHashes();
    hashes.current.forEach(function(currentHash) {
        var cls = (hashes.previous && hashes.previous.includes(currentHash)) ? "previous" : "current";
        document.getElementById(currentHash).className += " " + cls;
        console.log(currentHash + " - " + cls);
    });
});


