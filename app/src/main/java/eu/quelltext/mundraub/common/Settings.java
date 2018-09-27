package eu.quelltext.mundraub.common;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import eu.quelltext.mundraub.BuildConfig;
import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.api.API;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;
import eu.quelltext.mundraub.initialization.Permissions;
import eu.quelltext.mundraub.map.MundraubMapAPIForApp;
import eu.quelltext.mundraub.map.MundraubProxy;
import eu.quelltext.mundraub.map.OfflinePlantsMapAPI;
import okhttp3.OkHttpClient;

import static eu.quelltext.mundraub.common.Settings.ChangeListener.SETTINGS_CAN_CHANGE;

/*
    The settings are stored in the app preferences.
    One can attach to the settings changes.
    If a listener is against the new settings, they can deny committing them.
 */
public class Settings {

    public static final String API_ID_MUNDRAUB = "mundraub";
    public static final String API_ID_NA_OVOCE = "na-ovoce";
    public static final String API_ID_DUMMY = "dummy";
    public static final String API_ID_FRUITMAP = "fruitmap";
    public static final String API_ID_COMMUNITY = "community"; // only for markers


    private static final String PLANT_STORAGE_DIRECTORY_NAME = "eu.quelltext.mundraub";
    public static final String INVALID_HASH = "0000000000000000000000000000000000000000";
    public static final String COMMIT_HASH = "3a518993dfa2c1b3094c0847bc07ab22ec944054";
    private static final String PERMISSION_PREFIX = "askForPermission_";
    public static int COMMIT_SUCCESSFUL = ChangeListener.SETTINGS_CAN_CHANGE;

    public static String getShortHash(){
        return COMMIT_HASH.substring(0, 7);
    }

    public static boolean isRelease() {
        return !COMMIT_HASH.equals(INVALID_HASH);
    }

    private static Logger.Log log = Logger.newFor("Settings");
    private static SharedPreferences preferences = null;
    private static List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private static Activity activity = null;

    /* persistent variables for the settings
     * If you like to add new settings, please see useInsecureConnections
     * as an example for where these are used.
     * Here is an example commit ce993038e703548156f31c46b1fb30d38c9d2bc9:
     * https://github.com/niccokunzmann/mundraub-android/commit/ce993038e703548156f31c46b1fb30d38c9d2bc9
     */
    private static String useAPIId = API_ID_MUNDRAUB;
    private static boolean useInsecureConnections = false;
    private static boolean useCacheForPlants = true;
    private static File persistentPathForPlants = new File(Environment.getExternalStorageDirectory(), PLANT_STORAGE_DIRECTORY_NAME);
    private static Map<String, Boolean> permissionQuestion = new HashMap<String, Boolean>();
    private static boolean useErrorReport = true;
    private static boolean useOfflineMapAPI = false;
    private static boolean debugMundraubMapAPI = false;
    private static boolean vibrateWhenPlantIsInRange = false;
    private static Set<String> showCategories = new HashSet<>(Arrays.asList(API_ID_MUNDRAUB)); // https://stackoverflow.com/a/2041810/1320237
    private static boolean useFruitRadarNotifications = false;
    private static int radarPlantRangeMeters = 150;


    static {
        Initialization.provideActivityFor(new Initialization.ActivityInitialized() {
            @Override
            public void setActivity(Activity activity) {
                log.d("received Context", activity.toString());
                preferences = activity.getSharedPreferences("Settings", 0);
                Settings.activity = activity;
                load();
            }
        });
    }

    public static void print() {
        log.d("Build.VERSION.RELEASE", Build.VERSION.RELEASE);
        log.d("Build.VERSION.SDK_INT", Build.VERSION.SDK_INT);
        log.d("Build.MODEL", Build.MODEL);
        log.d("BuildConfig.VERSION_NAME", BuildConfig.VERSION_NAME);
        log.d("BuildConfig.DEBUG", BuildConfig.DEBUG);
        log.d("BuildConfig.VERSION_CODE", Integer.toString(BuildConfig.VERSION_CODE));
        log.d("COMMIT_HASH", COMMIT_HASH);
        log.d("Permissions.CAN_ASK_FOR_PERMISSIONS", Permissions.CAN_ASK_FOR_PERMISSIONS);
        log.d("useInsecureConnections", useInsecureConnections);
        log.d("useAPIId", useAPIId);
        log.d("useCacheForPlants", useCacheForPlants);
        log.d("useErrorReport", useErrorReport);
        log.d("useOfflineMapAPI", useOfflineMapAPI);
        log.d("debugMundraubMapAPI", debugMundraubMapAPI);
        log.d("vibrateWhenPlantIsInRange", vibrateWhenPlantIsInRange);
        log.d("useFruitRadarNotifications", useFruitRadarNotifications);
        log.d("radarPlantRangeMeters", radarPlantRangeMeters);
        log.d("showCategories", showCategoriesString());
        if (!useCacheForPlants) {
            log.d("persistentPathForPlants", persistentPathForPlants.toString());
        }
        for (String key : permissionQuestion.keySet()) {
            log.d(key, permissionQuestion.get(key));
        }
    }

    private static void load() {
        useAPIId = preferences.getString("useAPIId", useAPIId);
        useInsecureConnections = preferences.getBoolean("useInsecureConnections", useInsecureConnections);
        useCacheForPlants = preferences.getBoolean("useCacheForPlants", useCacheForPlants);
        persistentPathForPlants = new File(preferences.getString("persistentPathForPlants", persistentPathForPlants.toString()));
        useErrorReport = preferences.getBoolean("useErrorReport", useErrorReport);
        useOfflineMapAPI = preferences.getBoolean("useOfflineMapAPI", useOfflineMapAPI);
        debugMundraubMapAPI = preferences.getBoolean("debugMundraubMapAPI", debugMundraubMapAPI);
        vibrateWhenPlantIsInRange = preferences.getBoolean("vibrateWhenPlantIsInRange", vibrateWhenPlantIsInRange);
        useFruitRadarNotifications = preferences.getBoolean("useFruitRadarNotifications", useFruitRadarNotifications);
        radarPlantRangeMeters = preferences.getInt("radarPlantRangeMeters", radarPlantRangeMeters);
        String s = preferences.getString("showCategories", showCategoriesString());
        String[] l = StringUtils.split(s, ",");
        showCategories = new HashSet<String>(Arrays.asList(l));
        // load the permission questions
        permissionQuestion.clear();
        for (String key : preferences.getAll().keySet()) {
            if (key.startsWith(PERMISSION_PREFIX)) {
                permissionQuestion.put(key, preferences.getBoolean(key, true));
            }
        }
        // notify listeners about load
        for (ChangeListener listener : listeners) {
            listener.settingsChanged();
        }
        print();
    }

    @Nullable
    private static String showCategoriesString() {
        return StringUtils.join(showCategories.toArray(), ",");
    }

    private static int commit() {
        if (hasPreferences()) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("useAPIId", useAPIId);
            editor.putBoolean("useInsecureConnections", useInsecureConnections);
            editor.putBoolean("useCacheForPlants", useCacheForPlants);
            editor.putString("persistentPathForPlants", persistentPathForPlants.toString());
            editor.putBoolean("useErrorReport", useErrorReport);
            editor.putBoolean("useOfflineMapAPI", useOfflineMapAPI);
            editor.putBoolean("debugMundraubMapAPI", debugMundraubMapAPI);
            editor.putBoolean("vibrateWhenPlantIsInRange", vibrateWhenPlantIsInRange);
            editor.putBoolean("useFruitRadarNotifications", useFruitRadarNotifications);
            editor.putInt("radarPlantRangeMeters", radarPlantRangeMeters);
            editor.putString("showCategories", showCategoriesString());
            for (String key: permissionQuestion.keySet()) {
                editor.putBoolean(key, permissionQuestion.get(key));
            }
            for (ChangeListener listener : listeners) {
                int result = listener.settingsChanged();
                if (result != SETTINGS_CAN_CHANGE) {
                    log.d("commit aborted", activity.getResources().getString(result));
                    load();
                    return result;
                };
            }
            editor.commit();
        }
        print();
        return COMMIT_SUCCESSFUL;
    }


    private static boolean hasPreferences() {
        return preferences != null;
    }

    private static boolean hasActivity() {
        return activity != null;
    }

    public static String getAPIId() {
        return useAPIId;
    }

    public static int useAPI(API api) {
        useAPIId = api.id();
        return commit();
    }

    public static boolean useInsecureConnections() {
        return useInsecureConnections;
    }

    public static int useInsecureConnections(boolean insecure) {
        useInsecureConnections = insecure;
        return commit();
    }

    public static File getPersistentPlantDirectory() {
        File directory;
        if (useCacheForPlants && hasPreferences()) {
            directory = new File(activity.getCacheDir(), PLANT_STORAGE_DIRECTORY_NAME);
        } else {
            // from https://stackoverflow.com/questions/7887078/android-saving-file-to-external-storage#7887114
            directory = persistentPathForPlants;
        }
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                if (!useCacheForPlants && hasActivity()) {
                    // The directory could not be created
                    Permissions.of(activity).WRITE_EXTERNAL_STORAGE.check();
                }
            }
        }
        return directory;
    }

    public static int useCacheForPlants(boolean isChecked) {
        useCacheForPlants = isChecked;
        return commit();
    }

    public static boolean useErrorReport() {
        return useErrorReport;
    }

    public static int useErrorReport(boolean isChecked) {
        useErrorReport = isChecked;
        return commit();
    }

    public static boolean useOfflineMapAPI() {
        return useOfflineMapAPI;
    }

    public static int useOfflineMapAPI(boolean isChecked) {
        useOfflineMapAPI = isChecked;
        return commit();
    }

    public static String hostForMundraubAPI() {
        return debugMundraubMapAPI() ? "0.0.0.0" : "localhost";
    }

    public static boolean debugMundraubMapAPI() {
        return debugMundraubMapAPI;
    }

    public static int debugMundraubMapAPI(boolean isChecked) {
        debugMundraubMapAPI = isChecked;
        return commit();
    }

    public static boolean vibrateWhenPlantIsInRange() {
        return vibrateWhenPlantIsInRange;
    }

    public static int vibrateWhenPlantIsInRange(boolean isChecked) {
        vibrateWhenPlantIsInRange = isChecked;
        return commit();
    }

    public static boolean useCacheForPlants() {
        return useCacheForPlants;
    }

    public static boolean canAskForPermissionNamed(String permissionName) {
        String key = permissionKeyFromName(permissionName);
        if (permissionQuestion.containsKey(key)) {
            return permissionQuestion.get(key);
        }
        return true;
    }


    public static int canAskForPermissionNamed(String permissionName, boolean canAsk) {
        String key = permissionKeyFromName(permissionName);
        permissionQuestion.put(key, canAsk);
        return commit();
    }


    private static String permissionKeyFromName(String permissionName) {
        return PERMISSION_PREFIX + permissionName;
    }

    public static OkHttpClient getOkHttpClient() {
        return getOkHttpClient("SSL");
    }
    public static OkHttpClient getOkHttpClient(String SSLInstanceName) {
        // from https://stackoverflow.com/a/25992879/1320237
        try {

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (useInsecureConnections()) {
                // Create a trust manager that does not validate certificate chains
                final TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };

                // Install the all-trusting trust manager
                final SSLContext sslContext = SSLContext.getInstance(SSLInstanceName);
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                // Create an ssl socket factory with our all-trusting manager
                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                builder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }
            OkHttpClient okHttpClient = builder
                    .followRedirects(false) // from https://stackoverflow.com/a/29268150/1320237
                    .followSslRedirects(false)
                    .build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean useFruitRadarNotifications() {
        return useFruitRadarNotifications && useOfflineMapAPI();
    }

    public static int useFruitRadarNotifications(boolean used) {
        useFruitRadarNotifications = used;
        return commit();
    }

    public static int getRadarPlantRangeMeters() {
        return radarPlantRangeMeters;
    }

    public static int getRadarGPSPrecisionMeters() {
        return getRadarPlantRangeMeters() / 2;
    }

    /*
     * Prevent markers from showing up and being removed all the time.
     */
    public static double getRadarPlantMaximumRangeMeters() {
        return getRadarPlantRangeMeters() + Settings.getRadarGPSPrecisionMeters() * 2;
    }

    public static int setRadarPlantRangeMeters(int meters) {
        radarPlantRangeMeters = meters;
        return commit();
    }

    public static long vibrationMillisecondsForPlantInRange() {
        return 500;
    }

    public interface ChangeListener {
        int SETTINGS_CAN_CHANGE = R.string.settings_can_change;
        int settingsChanged();
    }

    public static void onChange(ChangeListener listener) {
        listeners.add(listener);
    }

    public static MundraubProxy getMundraubMapProxy() {
        if (useOfflineMapAPI()) {
            return new OfflinePlantsMapAPI();
        } else {
            return new MundraubMapAPIForApp();
        }
    }

    public static int showCategory(String apiId, boolean checked) {
        if (checked) {
            showCategories.add(apiId);
        } else {
            showCategories.remove(apiId);
        }
        return commit();
    }

    public static boolean showCategory(String apiId) {
        return showCategories.contains(apiId);
    }

}
