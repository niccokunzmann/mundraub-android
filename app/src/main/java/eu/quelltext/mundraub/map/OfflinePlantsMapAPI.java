package eu.quelltext.mundraub.map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import eu.quelltext.mundraub.common.Helper;

/*
 * Use the local databse to get the markers and not the online data.
 */
public class OfflinePlantsMapAPI extends MundraubMapAPIForApp {

    private static final String JSON_BBOX = "bbox";
    private static final String JSON_FEATURES = "features";


    @Override
    protected byte[] getResponseBytesFromPlantMarkerQuery(String queryParameterString) throws IOException, JSONException {
        Map<String, List<String>> parameters = Helper.splitQuery(queryParameterString);
        if (!parameters.containsKey(JSON_BBOX)) {
            return "{\"error\":\"bbox parameter is required\"}".getBytes("UTF-8"); // encoding from https://stackoverflow.com/a/5729823
        }
        String[] bbox = parameters.get(JSON_BBOX).get(0).split(",");
        if (bbox.length != 4) {
            return "{\"error\":\"bbox parameter needs 4 floats\"}".getBytes("UTF-8"); // encoding from https://stackoverflow.com/a/5729823
        }
        double minLon = Double.parseDouble(bbox[0]); // see map/requests.js
        double minLat = Double.parseDouble(bbox[1]);
        double maxLon = Double.parseDouble(bbox[2]);
        double maxLat = Double.parseDouble(bbox[3]);
        JSONArray markers = PlantsCache.getPlantsInBoundingBox(minLon, minLat, maxLon, maxLat);
        JSONObject result = new JSONObject();
        result.put(JSON_FEATURES, result);
        return result.toString().getBytes("UTF-8"); // encoding from https://stackoverflow.com/a/5729823
    }
}
