/*
 * This file provides logging functionality.
 */

if (window["webViewLogger"] != undefined) {
    log = {
        "log": function(string) {
            var result;
            if (typeof string == string) {
                result = string;
            } else {
                try {
                    result = JSON.stringify(string);
                } catch (error) {
                    result = "" + string;
                }
            }
            webViewLogger.log(result);
        }
    };
    log.log("Using logger provided by the web view.");
} else {
    log = {
        "log": function(string) {
            console.log(string);
        }
    };
    log.log("Using console logger.");
}