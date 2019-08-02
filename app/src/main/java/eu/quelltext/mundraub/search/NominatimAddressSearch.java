package eu.quelltext.mundraub.search;

import eu.quelltext.mundraub.api.progress.Progress;

public class NominatimAddressSearch implements IAddressSearch {

    private final INominatimInteraction interaction;
    private Progress progress;
    private Observer observer = new NullObserver();

    public NominatimAddressSearch(INominatimInteraction interaction) {
        this.interaction = interaction;
    }

    @Override
    public void notifyAboutChanges(Observer observer) {
        this.observer = observer;
    }

    @Override
    public AddressSearchResult get(int position) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void search(String text) {
        interaction.search(text, new INominatimInteraction.INominatimCallback() {
            @Override
            public void onResult(String result) {
                loadSearchResultFrom(result);
            }
            @Override
            public void onError(int errorId) {
                observer.onSearchError(errorId);
            }
        });
    }

    public void loadSearchResultFrom(String result) {

    }

}
