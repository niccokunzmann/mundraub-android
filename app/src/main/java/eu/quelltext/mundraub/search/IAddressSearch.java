package eu.quelltext.mundraub.search;

public interface IAddressSearch {

    void notifyAboutChanges(Observer observer);

    AddressSearchResult get(int position);

    int size();

    void search(String text);

    interface Observer {
        void onNewSearchResults(IAddressSearch addressSearch);
        void onSearchError(int errorId);
    }
}
