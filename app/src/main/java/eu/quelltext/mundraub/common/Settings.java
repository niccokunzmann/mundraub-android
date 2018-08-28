package eu.quelltext.mundraub.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import eu.quelltext.mundraub.BuildConfig;
import eu.quelltext.mundraub.error.Logger;
import eu.quelltext.mundraub.initialization.Initialization;

public class Settings {

    public static final String INVALID_HASH = "0000000000000000000000000000000000000000";
    public static final String COMMIT_HASH = "873fb12006828a72e88cdcd229406916db738a95";

    private static Logger.Log log = Logger.newFor("Settings");
    private static SharedPreferences preferences = null;

    private static boolean useMundraubAPI = true;
    private static boolean useInsecureConnections = false;


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
        log.d("useInsecureConnections", useInsecureConnections);
        log.d("useMundraubAPI", useMundraubAPI);
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
        useMundraubAPI = preferences.getBoolean("useMundraubAPI", useMundraubAPI);
        useInsecureConnections = preferences.getBoolean("useInsecureConnections", useInsecureConnections);
    }

    private static void commit() {
        if (hasPreferences()) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("useMundraubAPI", useMundraubAPI);
            editor.putBoolean("useInsecureConnections", useInsecureConnections);
            editor.commit();
        }
        print();
    }

    public static boolean useMundraubAPI() {
        return useMundraubAPI;
    }

    public static void useMundraubAPI(boolean useMundraub) {
        useMundraubAPI = useMundraub;
        commit();
    }

    public static boolean useInsecureConnections() {
        return useInsecureConnections;
    }

    public static void useInsecureConnections(boolean insecure) {
        useInsecureConnections = insecure;
        commit();
    }
}
