package eu.quelltext.mundraub;

public class Settings {

    public final String INVALID_HASH = "0000000000000000000000000000000000000000";
    public final String COMMIT_HASH = INVALID_HASH;

    public String getShortHash(){
        return COMMIT_HASH.substring(0, 7);
    }

    public boolean isRelease() {
        return !COMMIT_HASH.equals(INVALID_HASH);
    }
}
