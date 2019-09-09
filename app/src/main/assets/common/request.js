/*
 * General function to request JSON.
 */
function sendRequest(url, onSuccess, onError){
    // see https://developer.mozilla.org/en-US/docs/Learn/HTML/Forms/Sending_forms_through_JavaScript
    var XHR = new XMLHttpRequest();
    if (!onError) {
        onError = function() {
            log.log("ERROR: " + url + " failed.");
        }
    }
    // Define what happens on successful data submission
    XHR.addEventListener('load', function(event) {
        if (event.target.status == 200) {
            if (onSuccess) {
                try {
                    var json = JSON.parse(XHR.responseText);
                } catch (e) {
                    onError(event, e);
                    return;
                }
                onSuccess(json, event);
            }
        } else {
            onError(event);
        }
    });

    // Define what happens in case of error
    XHR.addEventListener('error', function(event) {
        onError(event);
    });

    // Set up our request
    XHR.open('GET', url, true);
    XHR.send(null);
}

