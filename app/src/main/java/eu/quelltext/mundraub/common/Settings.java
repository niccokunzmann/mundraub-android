package eu.quelltext.mundraub.common;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.quelltext.mundraub.BuildConfig;
import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;
import eu.quelltext.mundraub.initialization.Permissions;

import static eu.quelltext.mundraub.common.Settings.ChangeListener.SETTINGS_CAN_CHANGE;

/*
    The settings are stored in the app preferences.
    One can attach to the settings changes.
    If a listener is against the new settings, they can deny committing them.
 */
public class Settings {

    private static final String PLANT_STORAGE_DIRECTORY_NAME = "eu.quelltext.mundraub";
    public static final String INVALID_HASH = "0000000000000000000000000000000000000000";
    public static final String COMMIT_HASH = "2a650a2c22df80a68380aec08df9f897202c8132";
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


    public interface ChangeListener {
        int SETTINGS_CAN_CHANGE = R.string.settings_can_change;
        int settingsChanged();
    }

    public static void onChange(ChangeListener listener) {
        listeners.add(listener);
    }
}
