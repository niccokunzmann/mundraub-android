package eu.quelltext.mundraub.search;


import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import eu.quelltext.mundraub.map.position.IPosition;
import eu.quelltext.mundraub.map.position.Position;

/* This class is used to represent search results based on Nominatim.
 * see https://nominatim.openstreetmap.org/search?q=Potsdam&format=json
 */
public class AddressSearchResult implements Comparable<AddressSearchResult> {

    private static final String NOMINATIM_IMPORTANCE = "importance";
    private static final String NOMINATIM_DISPLAY_NAME = "display_name";
    private static final String NOMINATIM_LONGITUDE = "lon";
    private static final String NOMINATIM_LATITUDE = "lat";


    private final double importance;
    private final String displayName;
    private final String userInput;
    private final IPosition position;

    public AddressSearchResult(double importance, String displayName, String userInput, IPosition position) {
        this.importance = importance;
        this.displayName = displayName;
        this.userInput = userInput;
        this.position = position;
    }

    public static AddressSearchResult fromNominatim(JSONObject json, String searchTerm) throws JSONException {
        IPosition position = new Position(
                Double.parseDouble(json.getString(NOMINATIM_LONGITUDE)),
                Double.parseDouble(json.getString(NOMINATIM_LATITUDE))
        );
        return new AddressSearchResult(
                json.getDouble(NOMINATIM_IMPORTANCE),
                json.getString(NOMINATIM_DISPLAY_NAME),
                searchTerm,
                position
            );
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public int compareTo(@NonNull AddressSearchResult other) {
        return Double.compare(importance, other.importance);
    }

    public IPosition getPosition() {
        return position;
    }

    public String getSearchTerm() {
        return userInput;
    }
}
