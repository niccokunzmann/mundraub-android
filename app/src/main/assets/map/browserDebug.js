// from http://www.alanwood.net/demos/browserinfo.html
// debug the browser version

console.log("-------------- Browser -------------- ");
console.log("navigator.userAgent: " + navigator.userAgent);

window.addEventListener("load", function() {
    log.log("-------------- Browser -------------- ");
    log.log("navigator.userAgent: " + navigator.userAgent);
});
