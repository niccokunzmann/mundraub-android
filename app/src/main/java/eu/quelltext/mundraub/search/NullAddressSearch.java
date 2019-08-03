package eu.quelltext.mundraub.search;

public class NullAddressSearch implements IAddressSearch {
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

    @Override
    public void search(String text) {
    }
}
