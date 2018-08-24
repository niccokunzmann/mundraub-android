package eu.quelltext.mundraub;

import eu.quelltext.mundraub.error.Logger;

public class Settings {

    public static final String INVALID_HASH = "0000000000000000000000000000000000000000";
    public static final String COMMIT_HASH = "48c5f0d3266ae02f9b6b92ec906c0f742b6343d0";

    public static String getShortHash(){
        return COMMIT_HASH.substring(0, 7);
    }

    public static boolean isRelease() {
        return !COMMIT_HASH.equals(INVALID_HASH);
    }

    public static void print() {
        Logger.Log log = Logger.newFor("Settings");
        log.d("BuildConfig.VERSION_NAME", BuildConfig.VERSION_NAME);
        log.d("BuildConfig.DEBUG", Boolean.toString(BuildConfig.DEBUG));
        log.d("BuildConfig.VERSION_CODE", Integer.toString(BuildConfig.VERSION_CODE));
        log.d("COMMIT_HASH", COMMIT_HASH);
    }
}
