package eu.quelltext.mundraub;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import eu.quelltext.mundraub.map.MapUrl;
import eu.quelltext.mundraub.map.position.BoundingBox;
import eu.quelltext.mundraub.map.position.IPosition;
import eu.quelltext.mundraub.map.position.Position;
import eu.quelltext.mundraub.search.AddressSearchResult;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AddressSearchResultTest {

    private static final IPosition POSITION = new Position(0,0);
    private static final String JSON_TABARZ = "{\"place_id\":198722301,\"licence\":\"Data © OpenStreetMap contributors, ODbL 1.0. https://osm.org/copyright\",\"osm_type\":\"relation\",\"osm_id\":2816590,\"boundingbox\":[\"50.8327664\",\"50.8942435\",\"10.463545\",\"10.5413258\"],\"lat\":\"50.8761647\",\"lon\":\"10.5161433\",\"display_name\":\"Bad Tabarz, Landkreis Gotha, Thuringia, 99891, Germany\",\"class\":\"boundary\",\"type\":\"administrative\",\"importance\":0.44999999999999996,\"icon\":\"https://nominatim.openstreetmap.org/images/mapicons/poi_boundary_administrative.p.20.png\"}";
    private static final AddressSearchResult A1 = new AddressSearchResult(11, "Test1", "Te", POSITION);
    private static final AddressSearchResult A1_2 = new AddressSearchResult(11, "Test1", "Te", new Position(0,0));
    private static final AddressSearchResult A2 = new AddressSearchResult(11, "nase", "Nasupi", POSITION);
    private static final AddressSearchResult A3 = new AddressSearchResult(11, "LALALATe", "Teheran", POSITION);

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

    @Test
    public void testBoundingBoxIsSetToExtent() throws JSONException {
        JSONObject json = new JSONObject(JSON_TABARZ);
        AddressSearchResult a = AddressSearchResult.fromNominatim(json, "Tabarz");
        MapUrl url = a.asMapUrl();
        String[] extent = url.getString("extent").split(",");
        double[] values = {10.463545,50.8327664,10.5413258,50.8942435};
        for (int i = 0; i < 4; i++) {
            assertEquals(Double.parseDouble(extent[i]), values[i], 0.0000001);
        }
    }

    public void testDisplayNameIsUsedForMatch() {
        assertTrue(A1.matches("Test1"));
        assertTrue(A1.matches("test1"));
        assertTrue(A1.matches("1"));
        assertFalse(A2.matches("Test1"));
        assertFalse(A2.matches("test1"));
        assertFalse(A2.matches("1"));
        assertTrue(A2.matches("NASE"));
        assertTrue(A3.matches("ate"));
        assertTrue(A3.matches("laLA"));
        assertFalse(A3.matches("LALALALA"));
    }

    @Test
    public void testUserInputIsUsedForMatch() {
        assertTrue(A1.matches("Te"));
        assertTrue(A1.matches("T"));
        assertFalse(A1.matches("TestTest"));
        assertTrue(A2.matches("Nasu"));
        assertTrue(A2.matches("Nas"));
        assertTrue(A2.matches("I"));
        assertFalse(A2.matches("II"));
        assertTrue(A3.matches("heran"));
        assertTrue(A3.matches("Te"));
        assertFalse(A3.matches("RRan"));
    }

    @Test
    public void testEquality() {
        assertEquals(A1, A1);
        assertEquals(A2, A2);
        assertNotEquals(A1, A2);
        assertNotEquals(A2, A1);
        assertEquals(A1, A1_2);
        assertEquals(A1.hashCode(), A1_2.hashCode());
    }

    @Test
    public void testSerialize() throws JSONException {
        AddressSearchResult A2_2 = AddressSearchResult.fromJSON(A2.toJSON());
        AddressSearchResult A3_2 = AddressSearchResult.fromJSON(A3.toJSON());
        assertEquals(A2, A2_2);
        assertEquals(A3, A3_2);
    }

    @Test
    public void testSerializeBoundingBox() throws JSONException {
        AddressSearchResult a = new AddressSearchResult(11, "Test1", "Te", POSITION);
        a.setBoundingBox(BoundingBox.fromNESW(0,1,2,3));
        AddressSearchResult a_2 = AddressSearchResult.fromJSON(a.toJSON());
        assertEquals(a.asMapUrl().toString(), a_2.asMapUrl().toString());
    }
}
