package eu.quelltext.mundraub.api;

public class DummyAPI extends API {
    public Boolean login(String username, String password) {
        try {
            // Simulate network access.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            return false;
        }
        return username.equals("test") && password.equals("test");
    }
}
