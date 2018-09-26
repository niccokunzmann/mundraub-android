package eu.quelltext.mundraub.activities.map;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.util.Locale;

import eu.quelltext.mundraub.activities.MundraubBaseActivity;
import eu.quelltext.mundraub.common.Settings;
import eu.quelltext.mundraub.map.MapUrl;
import eu.quelltext.mundraub.map.MundraubProxy;
import eu.quelltext.mundraub.plant.Plant;

public class MapBaseActivity extends MundraubBaseActivity {

    protected WebView webView = null;
    private MundraubProxy apiProxy;

    private static final long MAXIMUM_TIME_TO_WAIT_FOR_GPS_TO_POSITION_THE_MAP = 1000000 * 5;

    protected void initializeWebView(int webViewId) {
        webView = (WebView) findViewById(webViewId);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // from https://stackoverflow.com/a/8846105/1320237
        // from https://stackoverflow.com/a/32587047/1320237
        webSettings.setBuiltInZoomControls(false);
        //webSettings.setDisplayZoomControls(false);
        // from https://stackoverflow.com/a/6255353/1320237
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        // persist the gelocation database, see https://stackoverflow.com/a/5423026
        webSettings.setGeolocationDatabasePath( this.getCacheDir().getPath() );
        webSettings.setAppCacheEnabled(true); // https://stackoverflow.com/a/8921072
        webSettings.setDatabaseEnabled(true); // https://stackoverflow.com/a/8921072
        webSettings.setDomStorageEnabled(true); // https://stackoverflow.com/a/8921072
        webSettings.setUserAgentString(webSettings.getUserAgentString() + " | language: " + Locale.getDefault().getLanguage()); // passthe user language from https://stackoverflow.com/a/9380140/1320237

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // from https://stackoverflow.com/a/30294054/1320237
                log.d("WebView", consoleMessage.sourceId() + " at line " +
                        consoleMessage.lineNumber() + ": " + consoleMessage.message());
                return true;
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                getPermissions().ACCESS_FINE_LOCATION.askIfNotGranted();
                callback.invoke(origin, true, false); // from https://stackoverflow.com/a/5423026
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                if (Build.VERSION.SDK_INT < 21) {
                    // CookieSyncManager is deprecated since API level 21 https://stackoverflow.com/a/47913011
                    android.webkit.CookieSyncManager.getInstance().sync(); // from https://stackoverflow.com/a/8390280
                }
            }
        });
        apiProxy = Settings.getMundraubMapProxy();
        Settings.onChange(new Settings.ChangeListener() {
            @Override
            public int settingsChanged() {
                apiProxy.stop();
                apiProxy = Settings.getMundraubMapProxy();
                return SETTINGS_CAN_CHANGE;
            }
        });
        getPermissions().INTERNET.askIfNotGranted();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            apiProxy.start();
        } catch (IOException e) {
            log.printStackTrace(e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        apiProxy.stop();
    }

    protected void openMapAtPosition(Plant.Position position) {
        openMapAtPosition(position.getLongitude(), position.getLatitude());
    }

    protected void openMapAtPosition(double[] position) {
        openMapAtPosition(position[0], position[1]);
    }

    protected void openMapAtPosition(double longitude, double latitude) {
        MapUrl url = new MapUrl(longitude, latitude);
        log.d("open map at position", url.toString());
        webView.loadUrl(url.getUrl());
    }

    public MapUrl getUrl() {
        return new MapUrl(webView.getUrl());
    }

    @SuppressLint("MissingPermission")
    protected void openMapAtGPSPositionOrLastPlantOrDefault() {
        openMapAtLastPlantOrDefault();
        final LocationManager locationManager = createLocationManager();
        if (locationManager == null) {
            return;
        }
        final long start = System.nanoTime();
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 50, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        locationManager.removeUpdates(this);
                        if (System.nanoTime() - start < MAXIMUM_TIME_TO_WAIT_FOR_GPS_TO_POSITION_THE_MAP) {
                            openMapAtPosition(location.getLongitude(), location.getLatitude());
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                }
        );
    }

    protected void openMapAtLastPlantOrDefault() {
        Plant.Position position = Plant.getAPositionNearAPlantForTheMap();
        openMapAtPosition(position);
    }

}
