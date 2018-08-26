package eu.quelltext.mundraub.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import eu.quelltext.mundraub.BuildConfig;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;

public class Settings {

    public static final String INVALID_HASH = "0000000000000000000000000000000000000000";
    public static final String COMMIT_HASH = INVALID_HASH;

    private static Logger.Log log = Logger.newFor("Settings");
    private static SharedPreferences preferences = null;

    private static boolean settingUseDummyAPI = false;


    static {
        Initialization.provideContextFor(new Initialization.ContextInitialized() {
            @Override
            public void setContext(Context context) {
                log.d("received Context", context.toString());
                preferences = context.getSharedPreferences("Settings", 0);
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
    }


    private static boolean hasPreferences() {
        return preferences != null;
    }

    public static String getShortHash(){
        return COMMIT_HASH.substring(0, 7);
    }

    public static boolean isRelease() {
        return !COMMIT_HASH.equals(INVALID_HASH);
    }

    private static void load() {
        settingUseDummyAPI = preferences.getBoolean("useDummyAPI", settingUseDummyAPI);
    }

    private static void commit() {
        if (hasPreferences()) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("useDummyAPI", settingUseDummyAPI);
            editor.commit();
        }
    }


    public static boolean isUsingTheDummyAPI() {
        return settingUseDummyAPI;
    }

    public static boolean isUsingTheMundraubAPI() {
        return !settingUseDummyAPI;
    }

    public static void useTheMundraubAPI() {
        settingUseDummyAPI = false;
        commit();
    }

    public static void useTheDummyAPI() {
        settingUseDummyAPI = true;
        commit();
    }

    public static boolean useInsecureConnections() {
        return true;
    }
}
