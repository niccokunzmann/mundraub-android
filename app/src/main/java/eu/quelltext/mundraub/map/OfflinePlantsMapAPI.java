package eu.quelltext.mundraub.map;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import eu.quelltext.mundraub.common.Helper;
import eu.quelltext.mundraub.map.position.BoundingBox;

/*
 * Use the local databse to get the markers and not the online data.
 */
public class OfflinePlantsMapAPI extends MundraubMapAPIForApp {

    private static final String JSON_BBOX = "bbox";
    private static final String JSON_FEATURES = "features";


    @Override
    protected byte[] getResponseBytesFromPlantMarkerQuery(String queryParameterString) throws IOException, Exception /*JSONException*/ {
        Map<String, List<String>> parameters = Helper.splitQuery(queryParameterString);
        if (!parameters.containsKey(JSON_BBOX)) {
            return "{\"error\":\"bbox parameter is required\"}".getBytes("UTF-8"); // encoding from https://stackoverflow.com/a/5729823
        }
        String[] bbox = parameters.get(JSON_BBOX).get(0).split(",");
        if (bbox.length != 4) {
            return "{\"error\":\"bbox parameter needs 4 floats\"}".getBytes("UTF-8"); // encoding from https://stackoverflow.com/a/5729823
        }
        // example: https://mundraub.org/cluster/plant?bbox=4.801025390625001,50.28231945008158,18.072509765625004,51.74743863117572
        BoundingBox bBox = BoundingBox.fromWestSouthEastNorthArray(bbox); // see map/requests.js
        JSONArray markers = PlantsCache.getPlantsInBoundingBox(bBox);
        JSONObject result = new JSONObject();
        result.put(JSON_FEATURES, markers);
        return result.toString().getBytes("UTF-8"); // encoding from https://stackoverflow.com/a/5729823
    }
}
