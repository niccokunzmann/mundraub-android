package eu.quelltext.mundraub;

public class Settings {

    public final String INVALID_HASH = "0000000000000000000000000000000000000000";
    public final String COMMIT_HASH = "27ef49f2f04468ec22806503ffc100f5d329e77b";

    public String getShortHash(){
        return COMMIT_HASH.substring(0, 7);
    }

    public boolean isRelease() {
        return !COMMIT_HASH.equals(INVALID_HASH);
    }
}
