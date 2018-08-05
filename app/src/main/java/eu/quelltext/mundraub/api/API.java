package eu.quelltext.mundraub.api;

public abstract class API {
    public static final API get() {
        return new DummyAPI();
    }

    public abstract Boolean login(String username, String password);
}
