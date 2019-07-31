package eu.quelltext.mundraub.map;

import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.quelltext.mundraub.map.position.BoundingBoxCollection;

/*
 * This class is used for the interaction with the map trough the URL.
 */
public class MapUrl {

    public  static final String PATH_SATELITE = "/tiles/ArcGIS/";
    public  static final String PATH_OSM = "/tiles/osm/";

    private static final String CONFIG_BOXES = "boxes";
    private static final String CONFIG_EARTH_URL = "earthUrl";
    private static final String CONFIG_MAPNIK_URL = "mapnikUrl";
    private static final String CONFIG_CENTER_LAT = "centerLat";
    private static final String CONFIG_CENTER_LON = "centerLon";
    private static final String CONFIG_MARKER_LAT = "markerLat";
    private static final String CONFIG_MARKER_LON = "markerLon";
    private static final String CONFIG_BROWSER_GPS = "browserGPS";
    private static final String CONFIG_CREATE_BOXES = "createBoxes";
    private Map<String, String> configuration = new HashMap<String, String>();

    public MapUrl(double longitude, double latitude) {
        commonConfiguration();
        configuration.put(CONFIG_CENTER_LON, Double.toString(longitude));
        configuration.put(CONFIG_CENTER_LAT, Double.toString(latitude));
        configuration.put(CONFIG_MARKER_LON, Double.toString(longitude));
        configuration.put(CONFIG_MARKER_LAT, Double.toString(latitude));
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

    /* Return the Url as a string. */
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
        return getDouble(CONFIG_CENTER_LAT);
    }

    public double getLongitude() {
        return getDouble(CONFIG_CENTER_LON);
    }

    public double getDouble(String name) {
        String value = getString(name);
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {}
        }
        return Double.NaN;
    }

    public String getString(String name) {
        return configuration.get(name);
    }

    public boolean isValid() {
        return configuration.containsKey(CONFIG_CENTER_LON) && configuration.containsKey(CONFIG_CENTER_LAT);
    }

    public MapUrl serveTilesFromLocalhost(int port) {
        configuration.put(CONFIG_EARTH_URL, "http://localhost:" + port + PATH_SATELITE + "${z}/${y}/${x}");
        configuration.put(CONFIG_MAPNIK_URL, "http://localhost:" + port + PATH_OSM + "${z}/${y}/${x}");
        return this;
    }

    public MapUrl setOfflineAreaBoundingBoxes(BoundingBoxCollection bboxes) {
        configuration.put(CONFIG_BOXES, bboxes.toJSONString());
        return this;
    }

    public BoundingBoxCollection getOfflineAreaBoundingBoxes() {
        if (configuration.containsKey(CONFIG_BOXES)) {
            return BoundingBoxCollection.fromJSONString(configuration.get(CONFIG_BOXES));
        }
        else {
            return BoundingBoxCollection.empty();
        }

    }

    public MapUrl createBoxes() {
        configuration.put(CONFIG_CREATE_BOXES, "true");
        return this;
    }
}
