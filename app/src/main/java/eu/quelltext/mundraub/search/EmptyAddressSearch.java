package eu.quelltext.mundraub.search;

public class EmptyAddressSearch implements AddressSearch {
    @Override
    public void notifyAboutChanges(Observer observer) {
        observer.onNewSearchResults(this);
    }

    @Override
    public AddressSearchResult get(int position) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }
}
