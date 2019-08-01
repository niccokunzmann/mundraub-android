// from https://stackoverflow.com/a/24103596/1320237
var setCookie;
var getCookie;
var deleteCookie;

if (window["localCookieManager"]) {
    console.log("Using the app cookie manager.");
    setCookie = function (name, value) {
        console.log("setCookie " + name + "=" + value);
        localCookieManager.setCookie(name, value);
    }
    getCookie = function (name) {
        console.log("getCookie " + name);
        return localCookieManager.getCookie(name);
    }
    deleteCookie = function (name) {
        console.log("deleteCookie " + name);
        localCookieManager.deleteCookie(name);
    }
} else {
    console.log("Using the browser cookie functions.");
    setCookie = function (name,value) {
        document.cookie = name + "=" + (value || "") + "; path=/";
    }
    getCookie = function (name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for(var i=0;i < ca.length;i++) {
            var c = ca[i];
            while (c.charAt(0)==' ') c = c.substring(1,c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
        }
        return null;
    }
    deleteCookie = function (name) {   
        document.cookie = name+'=; Max-Age=-99999999;';  
    }
}

