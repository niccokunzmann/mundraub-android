// from https://github.com/CoderDojoPotsdam/regex-tutorial/blob/master/js/cookies.js
var exdays = 1000; // Cookies are only valid for one day

// source https://www.w3schools.com/js/js_cookies.asp
// this has the JSON addition: cvalue is an object

function setCookie(cname, cvalue) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    var expires = "expires=" + d.toGMTString();
    var value = btoa(JSON.stringify(cvalue));
    document.cookie = cname + "=" + value + ";" + expires + ";path=/";
}

function getCookie(cname) {
    var name = cname + "=";
    var decodedCookie = decodeURIComponent(document.cookie);
    var ca = decodedCookie.split(';');
    for(var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            var string = c.substring(name.length, c.length);
            if (!string) { return null; };
            var json = atob(string);
            if (!json) { return null; };
            try {
              return JSON.parse(json);
            } catch (e) {
              console.error(e);
              return null;
            }
        }
    }
    return null;
}

function deleteCookies() {
  // from http://stackoverflow.com/a/27374628/1320237
  document.cookie.split(";").forEach(function(c) { document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/"); });
  document.location =  document.location;
}