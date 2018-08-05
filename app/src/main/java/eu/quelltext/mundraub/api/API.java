package eu.quelltext.mundraub.api;

public abstract class API {

    private static API instance = new DummyAPI();

    public static final API instance() {
        return instance;
    }

    public abstract Boolean login(String username, String password);
}
