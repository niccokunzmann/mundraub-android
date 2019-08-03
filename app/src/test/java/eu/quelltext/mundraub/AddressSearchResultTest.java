package eu.quelltext.mundraub;

import org.junit.Test;

import eu.quelltext.mundraub.map.position.IPosition;
import eu.quelltext.mundraub.map.position.Position;
import eu.quelltext.mundraub.search.AddressSearchResult;

import static org.junit.Assert.assertEquals;

public class AddressSearchResultTest {

    private static final IPosition POSITION = new Position(0,0);

    @Test
    public void testCompareWithUnequalImportance() {
        AddressSearchResult a1 = new AddressSearchResult(1, "", "", POSITION);
        AddressSearchResult a2 = new AddressSearchResult(0, "", "", POSITION);
        assertEquals(1, a1.compareTo(a2));
        assertEquals(-1, a2.compareTo(a1));
    }

    @Test
    public void testCompareWithEqualImportance() {
        AddressSearchResult a1 = new AddressSearchResult(11, "1", "22", POSITION);
        AddressSearchResult a2 = new AddressSearchResult(11, "22", "222", POSITION);
        assertEquals(0, a1.compareTo(a2));
        assertEquals(0, a2.compareTo(a1));
    }

    @Test
    public void testMapUrlIsBuiltWithPosition() {
        AddressSearchResult a1 = new AddressSearchResult(11, "1", "22", POSITION);
        AddressSearchResult a2 = new AddressSearchResult(11, "1", "22", new Position(22, -11));
        assertEquals(0, a1.asMapUrl().getLatitude(), 0.00001);
        assertEquals(0, a1.asMapUrl().getLongitude(), 0.00001);
        assertEquals(-11, a2.asMapUrl().getLatitude(), 0.00001);
        assertEquals(22, a2.asMapUrl().getLongitude(), 0.00001);
    }
}
