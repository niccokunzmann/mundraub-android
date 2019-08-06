package eu.quelltext.mundraub.common;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

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
import eu.quelltext.mundraub.map.TilesCache;
import eu.quelltext.mundraub.map.position.BoundingBoxCollection;
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

    public static final String TILES_OSM = "osm";
    public static final String TILES_SATELLITE = "satellite";

    private static final String PLANT_STORAGE_DIRECTORY_NAME = "eu.quelltext.mundraub";
    public static final String INVALID_HASH = "0000000000000000000000000000000000000000";
    public static final String COMMIT_HASH = "ef4a3b295ca49f78b67ef0a1669bf9b752faaed7";
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

    public static File mapTilesCacheDirectory(Context context) {
        return new File(context.getCacheDir(), "tiles");
    }

    public static double getRadiusInMetersForCloseAndEqualPlantsNearby() {
        return 30;
    }

    private static class SynchronizedStringSet { // TODO: test

        private final String name;
        private Set<String> strings;

        public SynchronizedStringSet(String name, List<String> strings) {
            this.name = name;
            this.strings = new HashSet<String>(strings);
        }

        @Override
        public String toString() {
            return StringUtils.join(strings.toArray(), ",");
        }

        public void load() {
            String s = preferences.getString(key(), toString());
            String[] l = StringUtils.split(s, ",");
            strings = new HashSet<String>(Arrays.asList(l));
        }

        private String key() {
            return name;
        }

        public void saveTo(SharedPreferences.Editor editor) {
            editor.putString(key(), toString());
        }

        public void add(String string) {
            strings.add(string);
        }

        public void remove(String string) {
            strings.remove(string);
        }

        public boolean contains(String string) {
            return strings.contains(string);
        }

        public void setChecked(String apiId, boolean checked) {
            if (checked) {
                strings.add(apiId);
            } else {
                strings.remove(apiId);
            }
        }
    }

    /* persistent variables for the settings
     * If you like to add new settings, please see vibrateWhenPlantIsInRange
     * as an example for where to add a new variable.
     * Here is an example commit 1d8bf40aa68d71cd35eb65e0e25986f6a8a1913e:
     * https://github.com/niccokunzmann/mundraub-android/commit/1d8bf40aa68d71cd35eb65e0e25986f6a8a1913e#diff-6cf4fcc1ccb27f70ca10a1b54612d568
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
    private static SynchronizedStringSet showCategories = new SynchronizedStringSet("showCategories", Arrays.asList(API_ID_MUNDRAUB)); // array as list https://stackoverflow.com/a/2041810/1320237
    private static SynchronizedStringSet downloadMarkersFromAPI = new SynchronizedStringSet("downloadMarkersFromAPI", Arrays.asList(API_ID_MUNDRAUB, API_ID_NA_OVOCE, API_ID_FRUITMAP)); // array as list https://stackoverflow.com/a/2041810/1320237
    private static SynchronizedStringSet tilesToDownload = new SynchronizedStringSet("tilesToDownload", Arrays.asList(TILES_OSM));
    private static boolean useFruitRadarNotifications = false;
    private static int radarPlantRangeMeters = 150;
	private static int maximumDisplayedMarkers = 100;
    private static int maximumZoomLevelForOfflineMap = 16;
    private static boolean downloadMapTilesForZoomLevelsLowerThanMaximum = true;
    private static boolean userHasReadThePrivacyPolicy = false;
    private static BoundingBoxCollection offlineMapAreaBoundingBoxes = BoundingBoxCollection.empty();

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
		log.d("maximumDisplayedMarkers", maximumDisplayedMarkers);
        log.d("maximumZoomLevelForOfflineMap", maximumZoomLevelForOfflineMap);
        log.d("downloadMapTilesForZoomLevelsLowerThanMaximum", downloadMapTilesForZoomLevelsLowerThanMaximum);
        log.d("userHasReadThePrivacyPolicy", userHasReadThePrivacyPolicy);
        log.d("offlineMapAreaBoundingBoxes", offlineMapAreaBoundingBoxes.toJSONString());
        log.d("showCategories", showCategories.toString());
        log.d("downloadMarkersFromAPI", downloadMarkersFromAPI.toString());
        log.d("tilesToDownload", tilesToDownload.toString());
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
		maximumDisplayedMarkers = preferences.getInt("maximumDisplayedMarkers", maximumDisplayedMarkers);
        radarPlantRangeMeters = preferences.getInt("radarPlantRangeMeters", radarPlantRangeMeters);
        maximumZoomLevelForOfflineMap = preferences.getInt("maximumZoomLevelForOfflineMap", maximumZoomLevelForOfflineMap);
        downloadMapTilesForZoomLevelsLowerThanMaximum = preferences.getBoolean("downloadMapTilesForZoomLevelsLowerThanMaximum", downloadMapTilesForZoomLevelsLowerThanMaximum);
        userHasReadThePrivacyPolicy = preferences.getBoolean("userHasReadThePrivacyPolicy", userHasReadThePrivacyPolicy);
        offlineMapAreaBoundingBoxes = BoundingBoxCollection.fromJSONString(preferences.getString("offlineMapAreaBoundingBoxes", offlineMapAreaBoundingBoxes.toJSONString()));
        showCategories.load();
        downloadMarkersFromAPI.load();
        tilesToDownload.load();
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
			editor.putInt("maximumDisplayedMarkers", maximumDisplayedMarkers);
            editor.putInt("radarPlantRangeMeters", radarPlantRangeMeters);
            editor.putInt("maximumZoomLevelForOfflineMap", maximumZoomLevelForOfflineMap);
            editor.putBoolean("downloadMapTilesForZoomLevelsLowerThanMaximum", downloadMapTilesForZoomLevelsLowerThanMaximum);
            editor.putBoolean("userHasReadThePrivacyPolicy", userHasReadThePrivacyPolicy);
            editor.putString("offlineMapAreaBoundingBoxes", offlineMapAreaBoundingBoxes.toJSONString());
            showCategories.saveTo(editor);
            downloadMarkersFromAPI.saveTo(editor);
            tilesToDownload.saveTo(editor);
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
	
	public static int getMaximumDisplayedMarkers(){
		return maximumDisplayedMarkers;
	}
	
	public static int setMaximumDisplayedMarkers(int maxMarkers){
		maximumDisplayedMarkers = maxMarkers;
		return commit();
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

    public static int downloadMarkersFromAPI(String apiId, boolean checked) {
        downloadMarkersFromAPI.setChecked(apiId, checked);
        return commit();
    }

    public static boolean downloadMarkersFromAPI(String apiId) {
        return downloadMarkersFromAPI.contains(apiId);
    }

    public static int setDownloadMap(String mapId, boolean checked) {
        tilesToDownload.setChecked(mapId, checked);
        return commit();
    }

    public static boolean getDownloadMap(String mapId) {
        return tilesToDownload.contains(mapId);
    }

    public static int setOfflineAreaBoundingBoxes(BoundingBoxCollection offlineAreaBoundingBoxes) {
        offlineMapAreaBoundingBoxes = offlineAreaBoundingBoxes;
        return commit();
    }

    public static BoundingBoxCollection getOfflineAreaBoundingBoxes() {
        return offlineMapAreaBoundingBoxes;
    }

    public static List<TilesCache> getDownloadMaps() {
        List<TilesCache> caches = new ArrayList<>();
        if (getDownloadMap(TILES_OSM)) {
            caches.add(TilesCache.forOSM());
        }
        if (getDownloadMap(TILES_SATELLITE)) {
            caches.add(TilesCache.forSatellite());
        }
        return caches;
    }

    public static int[] getDownloadZoomLevels() {
        if (downloadMapTilesForZoomLevelsLowerThanMaximum()) {
            int[] levels = new int[maximumZoomLevelForOfflineMap + 1];
            for (int i = 0; i < levels.length; i++) {
                levels[i] = i;
            }
            return levels;
        }
        return new int[]{maximumZoomLevelForOfflineMap};
    }

    public static int downloadMapTilesForZoomLevelsLowerThanMaximum(boolean checked) {
        downloadMapTilesForZoomLevelsLowerThanMaximum = checked;
        return commit();
    }

    public static boolean downloadMapTilesForZoomLevelsLowerThanMaximum() {
        return downloadMapTilesForZoomLevelsLowerThanMaximum;
    }

    public static int setUserHasReadThePrivacyPolicy() {
        userHasReadThePrivacyPolicy = true;
        return commit();
    }

    public static boolean shouldAskTheUserToOpenThePrivacyPolicy() {
        return !userHasReadThePrivacyPolicy;
    }

    public static int userDidNotWantToViewThePolicyOnStart() {
        return setUserHasReadThePrivacyPolicy();
    }



}