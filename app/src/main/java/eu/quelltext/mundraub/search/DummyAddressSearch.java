package eu.quelltext.mundraub.search;

import eu.quelltext.mundraub.map.position.Position;

public class DummyAddressSearch implements IAddressSearch {
    private AddressSearchResult result;
    private Observer observer = new NullObserver();

    public DummyAddressSearch() {
        search("Potsdam");
    }

    @Override
    public void notifyAboutChanges(Observer observer) {
        this.observer = observer;
        observer.onNewSearchResults(this);
    }

    @Override
    public AddressSearchResult get(int position) {
        return result;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public void search(String text) {
        result = new AddressSearchResult(
                1, text, text,
                new Position(13.0591397, 52.4009309));
        observer.onNewSearchResults(this);
    }
}
