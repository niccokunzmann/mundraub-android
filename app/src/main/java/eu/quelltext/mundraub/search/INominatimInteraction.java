package eu.quelltext.mundraub.search;

public interface INominatimInteraction {
    void search(String text, INominatimCallback cb);

    interface INominatimCallback {
        void onResult(String result);
        void onError(int errorId);
    }
}
