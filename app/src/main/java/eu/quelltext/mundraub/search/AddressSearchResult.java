package eu.quelltext.mundraub.search;


import eu.quelltext.mundraub.map.position.IPosition;

/* This class is used to represent search results based on Nominatim.
 * see https://nominatim.openstreetmap.org/search?q=Potsdam&format=json
 */
public class AddressSearchResult {

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

    public String getDisplayName() {
        return displayName;
    }
}
