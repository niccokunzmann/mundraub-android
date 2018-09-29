package eu.quelltext.mundraub.map;

import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This class is used for the interaction with the map trough the URL.
 */
public class MapUrl {

    private Map<String, String> configuration = new HashMap<String, String>();

    public MapUrl(double longitude, double latitude) {
        configuration.put("lon", Double.toString(longitude));
        configuration.put("lat", Double.toString(latitude));
    }

    public MapUrl(String url) {
        super();
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
        return getDouble("lat");
    }

    public double getLongitude() {
        return getDouble("lon");
    }

    public double getDouble(String name) {
        return Double.parseDouble(getString(name));
    }

    public String getString(String name) {
        return configuration.get(name);
    }

    public boolean isValid() {
        return configuration.containsKey("lon") && configuration.containsKey("lat");
    }
}
