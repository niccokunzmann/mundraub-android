/*
 * calculate the distance between to eartch coordinates in
 * longitude and latitude in JavaScript.
 *
 * We use the computational formula for points close to eachother,
 * the haversine formula.
 * https://en.wikipedia.org/wiki/Great-circle_distance#Computational_formulas
 */ 
 
var EARTH_RADIUS_KM = 6399.594; 

function deg2rad(degrees) {
    var radians = degrees * Math.PI / 180;
    return radians;
}

function distanceKM(lonlat1, lonlat2) {
    console.log("distanceKM", lonlat1, lonlat2);
    // convert from degree to radial
    var phi1 = deg2rad(lonlat1.lat);
    var phi2 = deg2rad(lonlat2.lat);
    var lambda1 = deg2rad(lonlat1.lon);
    var lambda2 = deg2rad(lonlat2.lon);
    // compute the deltas
    var dPhi = phi1 - phi2; // Δφ
    var dLambda = lambda1 - lambda2; // Δλ
    // compute haversine formula
    var dRoh = 2 * Math.asin(Math.sqrt(
            Math.pow(Math.sin(dPhi / 2), 2) +
            Math.cos(phi1) * Math.cos(phi2) *
                Math.pow(Math.sin(dLambda / 2), 2)
        ));
    var distance = EARTH_RADIUS_KM * dRoh;
    return distance;
}

function distanceString(lonlat1, lonlat2) {
    var distance = distanceKM(lonlat1, lonlat2);
    if (distance > 100) {
        return (Math.round(distance / 10) * 10).toFixed(0) + "km";
    } else if (distance > 10) {
        return distance.toFixed(0) + "km";
    } else if (distance > 1) {
        return distance.toFixed(1) + "km"
    } else if (distance > 0.03) {
        return (Math.round(distance * 100) * 10).toFixed(0) + "m";
    }
    return (distance * 1000).toFixed(0) + "m";
}

