package eu.quelltext.mundraub.map;

import java.net.MalformedURLException;
import java.net.URL;

import eu.quelltext.mundraub.error.ErrorAware;

/*
 * This class is used for the interaction with the map trough the URL.
 */
public class MapUrl extends ErrorAware {

    private double latitude;
    private double longitude;
    private final boolean valid;

    public MapUrl(double longitude, double latitude) {
        super();
        this.longitude = longitude;
        this.latitude = latitude;
        valid = true;
    }

    public MapUrl(String url_) {
        super();
        boolean valid1;
        URL url = null; // examples/fullScreen.html#11.523992844180245,47.30569859911609
        try {
            url = new URL(url_);
            String ref = url.getRef(); // 11.523992844180245,47.30569859911609
            if (ref == null) {
                ref = url.getQuery();
            }
            String[] parts = ref.split(","); // from http://stackoverflow.com/questions/3481828/ddg#3481842
            longitude = Double.parseDouble(parts[0]); // 11.523992844180245
            latitude = Double.parseDouble(parts[1]);  // 47.30569859911609
            valid1 = true;
        } catch (MalformedURLException e) {
            log.printStackTrace(e);
            log.e("invalid url", url_);
            valid1 = false;
        }
        valid = valid1;
    }

    public String getUrl() {
        return "file:///android_asset/map/examples/fullScreen.html?" + longitude + "," + latitude;
    }

    @Override
    public String toString() {
        return getUrl();
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isValid() {
        return valid;
    }
}
