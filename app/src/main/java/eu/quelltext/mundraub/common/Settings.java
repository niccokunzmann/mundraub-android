package eu.quelltext.mundraub.common;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import eu.quelltext.mundraub.BuildConfig;
import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;
import eu.quelltext.mundraub.initialization.Permissions;
import eu.quelltext.mundraub.map.MundraubMapAPIForApp;
import eu.quelltext.mundraub.map.MundraubProxy;
import okhttp3.OkHttpClient;

import static eu.quelltext.mundraub.common.Settings.ChangeListener.SETTINGS_CAN_CHANGE;

/*
    The settings are stored in the app preferences.
    One can attach to the settings changes.
    If a listener is against the new settings, they can deny committing them.
 */
public class Settings {

    private static final String PLANT_STORAGE_DIRECTORY_NAME = "eu.quelltext.mundraub";
    public static final String INVALID_HASH = "0000000000000000000000000000000000000000";
    public static final String COMMIT_HASH = "b900d04d1804006611d79aa250a678175d3323eb";
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

    /* persistent variables for the settings */
    private static boolean useMundraubAPI = true;
    private static boolean useInsecureConnections = false;
    private static boolean useCacheForPlants = true;
    private static File persistentPathForPlants = new File(Environment.getExternalStorageDirectory(), PLANT_STORAGE_DIRECTORY_NAME);
    private static Map<String, Boolean> permissionQuestion = new HashMap<String, Boolean>();

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
        log.d("useMundraubAPI", useMundraubAPI);
        log.d("useCacheForPlants", useCacheForPlants);
        if (!useCacheForPlants) {
            log.d("persistentPathForPlants", persistentPathForPlants.toString());
        }
        for (String key : permissionQuestion.keySet()) {
            log.d(key, permissionQuestion.get(key));
        }
    }

    private static void load() {
        useMundraubAPI = preferences.getBoolean("useMundraubAPI", useMundraubAPI);
        useInsecureConnections = preferences.getBoolean("useInsecureConnections", useInsecureConnections);
        useCacheForPlants = preferences.getBoolean("useCacheForPlants", useCacheForPlants);
        persistentPathForPlants = new File(preferences.getString("persistentPathForPlants", persistentPathForPlants.toString()));
        permissionQuestion.clear();
        for (String key : preferences.getAll().keySet()) {
            if (key.startsWith(PERMISSION_PREFIX)) {
                permissionQuestion.put(key, preferences.getBoolean(key, true));
            }
        }
        for (ChangeListener listener : listeners) {
            listener.settingsChanged();
        }
    }

    private static int commit() {
        if (hasPreferences()) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("useMundraubAPI", useMundraubAPI);
            editor.putBoolean("useInsecureConnections", useInsecureConnections);
            editor.putBoolean("useCacheForPlants", useCacheForPlants);
            editor.putString("persistentPathForPlants", persistentPathForPlants.toString());
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

    public static boolean useMundraubAPI() {
        return useMundraubAPI;
    }

    public static int useMundraubAPI(boolean useMundraub) {
        useMundraubAPI = useMundraub;
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
                final SSLContext sslContext = SSLContext.getInstance("SSL");
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


    public interface ChangeListener {
        int SETTINGS_CAN_CHANGE = R.string.settings_can_change;
        int settingsChanged();
    }

    public static void onChange(ChangeListener listener) {
        listeners.add(listener);
    }

    public static MundraubProxy getMundraubMapProxy() {
        try {
            return MundraubMapAPIForApp.getInstance();
        } catch (IOException e) {
            log.printStackTrace(e);
            log.e("Map proxy:", "could not create the map proxy");
            return new MundraubProxy() {

                private Logger.Log log = Logger.newFor(this);

                @Override
                public void start() {
                    log.d("DUMMY", "start");
                }

                @Override
                public void stop() {
                    log.d("DUMMY", "stop");
                }
            };
        }
    }
}
