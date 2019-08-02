package eu.quelltext.mundraub.search;

public interface AddressSearch {

    void notifyAboutChanges(Observer observer);

    AddressSearchResult get(int position);

    int size();

    interface Observer {
        void onNewSearchResults(AddressSearch addressSearch);
    }
}
