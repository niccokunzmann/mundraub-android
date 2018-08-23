package eu.quelltext.mundraub;

public class Settings {

    public final String INVALID_HASH = "0000000000000000000000000000000000000000";
    public final String COMMIT_HASH = "79b9f1a70adad1fda076700684ded3e86df0edf8";

    public String getShortHash(){
        return COMMIT_HASH.substring(0, 7);
    }

    public boolean isRelease() {
        return !COMMIT_HASH.equals(INVALID_HASH);
    }
}
