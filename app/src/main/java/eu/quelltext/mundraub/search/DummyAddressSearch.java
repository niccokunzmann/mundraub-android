package eu.quelltext.mundraub.search;

import eu.quelltext.mundraub.map.position.Position;

public class DummyAddressSearch implements AddressSearch {
    @Override
    public void notifyAboutChanges(Observer observer) {
        observer.onNewSearchResults(this);
    }

    @Override
    public AddressSearchResult get(int position) {
        return new AddressSearchResult(1, "Potsdam", "P",
                new Position(13.0591397, 52.4009309));
    }

    @Override
    public int size() {
        return 1;
    }
}
