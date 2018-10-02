package eu.quelltext.mundraub.map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.quelltext.mundraub.map.position.BoundingBox;

/*
 * This class is used for the interaction with the map trough the URL.
 */
public class MapUrl {

    public static final String CONFIG_BOXES = "boxes";
    public static final String CONFIG_EARTH_URL = "earthUrl";
    public static final String CONFIG_MAPNIK_URL = "mapnikUrl";
    public static final String CONFIG_LAT = "lat";
    public static final String CONFIG_LON = "lon";
    public static final String CONFIG_BROWSER_GPS = "browserGPS";
    private Map<String, String> configuration = new HashMap<String, String>();

    public MapUrl(double longitude, double latitude) {
        commonConfiguration();
        configuration.put("lon", Double.toString(longitude));
        configuration.put("lat", Double.toString(latitude));
    }

    private void commonConfiguration() {
        configuration.put(CONFIG_BROWSER_GPS, "false");
    }

    public MapUrl(String url) {
        String configuration = "";
        if (url.contains("#")) {
            configuration = url.substring(url.lastIndexOf("#") + 1);
        } else if (url.contains("?")) {
            configuration = url.substring(url.lastIndexOf("?") + 1);
        }
        String[] values = configuration.split("&");
        for (String value: values) {
            String[] splitValue = value.split("=");
            if (splitValue.length != 2) {
                continue;
            }
            this.configuration.put(URLDecoder.decode(splitValue[0]), URLDecoder.decode(splitValue[1]));
        }
        commonConfiguration();
    }

    public String getUrl() {
        List<String> query = new ArrayList<>();
        for (Map.Entry<String, String> entry : configuration.entrySet()) {
            query.add(URLEncoder.encode(entry.getKey()) + "=" + URLEncoder.encode(entry.getValue()));
        }
        return "file:///android_asset/map/examples/fullScreen.html?" + StringUtils.join(query.toArray(), "&");
    }

    @Override
    public String toString() {
        return getUrl();
    }

    public double getLatitude() {
        return getDouble(CONFIG_LAT);
    }

    public double getLongitude() {
        return getDouble(CONFIG_LON);
    }

    public double getDouble(String name) {
        return Double.parseDouble(getString(name));
    }

    public String getString(String name) {
        return configuration.get(name);
    }

    public boolean isValid() {
        return configuration.containsKey(CONFIG_LON) && configuration.containsKey(CONFIG_LAT);
    }

    public MapUrl serveTilesFromLocalhost(int port) {
        configuration.put(CONFIG_EARTH_URL, "http://localhost:" + port + "/tiles/ArcGIS/${z}/${y}/${x}");
        configuration.put(CONFIG_MAPNIK_URL, "http://localhost:" + port + "/tiles/osm/${z}/${y}/${x}");
        return this;
    }

    public MapUrl setOfflineAreaBoundingBoxes(List<BoundingBox> bboxes) {
        JSONArray bboxesJSON = new JSONArray();
        for (BoundingBox bbox : bboxes) {
            bboxesJSON.put(bbox.toJSON());
        }
        configuration.put(CONFIG_BOXES, bboxesJSON.toString());
        return this;
    }

    public List<BoundingBox> getOfflineAreaBoundingBoxes() {
        List<BoundingBox> bboxes = new ArrayList<>();
        if (configuration.containsKey(CONFIG_BOXES)) {
            try {
                JSONArray bboxesJSON = new JSONArray(configuration.get(CONFIG_BOXES));
                for (int i = 0; i < bboxesJSON.length(); i++) {
                        bboxes.add(BoundingBox.fromJSON(bboxesJSON.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace(); // will never happen I suppose
            }
        }
        return bboxes;
    }
}
