// Compatibility functions

if (!Array.prototype.includes) {
    Array.prototype.includes = function (element) {
        for (var i = 0; i < this.length; i++) {
            if (this[i] == element) {
                return true;
            }
        }
        return false;
    }
}

// indexOf from https://stackoverflow.com/a/5767357
Array.prototype.indexOf || (Array.prototype.indexOf = function(d, e) {
    var a;
    if (null == this) throw new TypeError('"this" is null or not defined');
    var c = Object(this),
        b = c.length >>> 0;
    if (0 === b) return -1;
    a = +e || 0;
    Infinity === Math.abs(a) && (a = 0);
    if (a >= b) return -1;
    for (a = Math.max(0 <= a ? a : b - Math.abs(a), 0); a < b;) {
        if (a in c && c[a] === d) return a;
        a++
    }
    return -1
});

// matchAll from http://cwestblog.com/2013/02/26/javascript-string-prototype-matchall/
String.prototype.matchAll || (String.prototype.matchAll = function(regexp) {
    var matches = [];
    this.replace(regexp, function() {
        var arr = ([]).slice.call(arguments, 0);
        var extras = arr.splice(-2);
        arr.index = extras[0];
        arr.input = extras[1];
        matches.push(arr);
    });
    var i = 0;
    return {
        next: function() {
            if (i < matches.length) {
                i++;
                return {
                    value: matches[i],
                    done: false
                };
            } else {
                return {
                    value: undefined,
                    done: true
                }
            }
        }
    };
});

