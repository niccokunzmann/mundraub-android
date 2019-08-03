package eu.quelltext.mundraub.search;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import eu.quelltext.mundraub.api.progress.Progress;

public class NominatimAddressSearch implements IAddressSearch {

    private final INominatimInteraction interaction;
    private Progress progress;
    private Observer observer = new NullObserver();
    private List<AddressSearchResult> content;

    public NominatimAddressSearch(INominatimInteraction interaction) {
        this.interaction = interaction;
        content = new ArrayList<>();
    }

    @Override
    public void notifyAboutChanges(Observer observer) {
        this.observer = observer;
    }

    @Override
    public AddressSearchResult get(int position) {
        return content.get(position);
    }

    @Override
    public int size() {
        return content.size();
    }

    @Override
    public void search(final String text) {
        interaction.search(text, new INominatimInteraction.INominatimCallback() {
            @Override
            public void onResult(String result) {
                loadSearchResultFrom(result, text);
            }
            @Override
            public void onError(int errorId) {
                observer.onSearchError(errorId);
            }
        });
    }

    public void loadSearchResultFrom(String resultString, String searchTerm) {
        JSONArray results;
        List<AddressSearchResult> searchResults = new ArrayList<>();
        try {
            results = new JSONArray(resultString);
            for (int i = 0; i < results.length(); i++) {
                JSONObject result = results.getJSONObject(i);
                searchResults.add(AddressSearchResult.fromNominatim(result, searchTerm));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        content = searchResults;
        observer.onNewSearchResults(this);
    }
}
