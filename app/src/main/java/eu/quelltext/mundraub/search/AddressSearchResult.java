package eu.quelltext.mundraub.search;


import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.quelltext.mundraub.map.MapUrl;
import eu.quelltext.mundraub.map.position.BoundingBox;
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
    private static final String NOMINATIM_BOUNDINGBOX = "boundingbox";

    private static final String JSON_IMPORTANCE = "importance";
    private static final String JSON_DISPLAY_NAME = "display-name";
    private static final String JSON_LONGITUDE = "lon";
    private static final String JSON_LATITUDE = "lat";
    private static final String JSON_USER_INPUT = "search-term";
    private static final String JSON_BOUNDINGBOX = "boundingbox";

    private final double importance;
    private final String displayName;
    private final String userInput;
    private final IPosition position;
    private BoundingBox boundingBox = null;

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
        AddressSearchResult result = new AddressSearchResult(
                json.getDouble(NOMINATIM_IMPORTANCE),
                json.getString(NOMINATIM_DISPLAY_NAME),
                searchTerm,
                position
        );
        BoundingBox bbox = BoundingBox.fromNominatim(json.getJSONArray(NOMINATIM_BOUNDINGBOX));
        result.setBoundingBox(bbox);
        return result;
    }

    public void setBoundingBox(BoundingBox bbox) {
        boundingBox = bbox;
    }

    public static AddressSearchResult fromJSON(JSONObject json) throws JSONException {
        AddressSearchResult result = new AddressSearchResult(
                json.getDouble(JSON_IMPORTANCE),
                json.getString(JSON_DISPLAY_NAME),
                json.getString(JSON_USER_INPUT),
                new Position(
                        json.getDouble(JSON_LONGITUDE),
                        json.getDouble(JSON_LATITUDE)
                )
        );
        if (json.has(JSON_BOUNDINGBOX)) {
            result.setBoundingBox(BoundingBox.fromJSON(json.getJSONObject(JSON_BOUNDINGBOX)));
        }
        return result;
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

    public MapUrl asMapUrl() {
        MapUrl url = new MapUrl(getPosition().getLongitude(), getPosition().getLatitude());
        if (boundingBox != null) {
            url.setExtent(boundingBox);
        }
        return url;
    }

    public boolean matches(String searchTerm) {
        String lowerSearchTerm = searchTerm.toLowerCase();
        return displayName.toLowerCase().contains(lowerSearchTerm) ||
                userInput.toLowerCase().contains(lowerSearchTerm);
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj instanceof AddressSearchResult) {
            AddressSearchResult other = (AddressSearchResult) obj;
            return other.compareTo(this) == 0 &&
                    other.getSearchTerm().equals(this.getSearchTerm()) &&
                    other.getDisplayName().equals(this.getDisplayName()) &&
                    other.getPosition().getLatitude() == this.getPosition().getLatitude() &&
                    other.getPosition().getLongitude() == this.getPosition().getLongitude();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getDisplayName().hashCode() ^ getSearchTerm().hashCode();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_DISPLAY_NAME, getDisplayName());
        json.put(JSON_LONGITUDE, getPosition().getLongitude());
        json.put(JSON_LATITUDE, getPosition().getLatitude());
        json.put(JSON_IMPORTANCE, importance);
        json.put(JSON_USER_INPUT, getSearchTerm());
        if (boundingBox != null) {
            json.put(JSON_BOUNDINGBOX, boundingBox.toJSON());
        }
        return json;
    }
}
