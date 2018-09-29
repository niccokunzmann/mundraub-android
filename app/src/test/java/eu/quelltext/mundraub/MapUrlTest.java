package eu.quelltext.mundraub;

import org.junit.Test;

import eu.quelltext.mundraub.map.MapUrl;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MapUrlTest {

    @Test
    public void testLongitudeAndLatitudeArePassedOn() {
        MapUrl mapUrl = new MapUrl(10, 11);
        String url = mapUrl.toString();
        MapUrl parsedMapUrl = new MapUrl(url);
        assertEquals(10, parsedMapUrl.getLongitude(), 0.1);
        assertEquals(11, parsedMapUrl.getLatitude(), 0.1);
    }

    @Test
    public void testCanUseUnknownParameters1() {
        MapUrl mapUrl = new MapUrl("?test=1");
        assertEquals("1", mapUrl.getString("test"));
        assertEquals(1.0, mapUrl.getDouble("test"), 0.1);
    }

    @Test
    public void testCanUseUnknownParameters2() {
        MapUrl mapUrl = new MapUrl("?test=2");
        assertEquals("2", mapUrl.getString("test"));
        assertEquals(2.0, mapUrl.getDouble("test"), 0.1);
    }

    @Test
    public void testHashIsPreferredBeforeAQuery() {
        MapUrl mapUrl = new MapUrl("?test=2#test=4");
        assertEquals("4", mapUrl.getString("test"));
    }

    @Test
    public void testUrlIsOnlyValidWithLongitudeAndLatitude() {
        assertFalse(new MapUrl("?test=2&test=4").isValid());
        assertFalse(new MapUrl("?lon=2&test=4").isValid());
        assertFalse(new MapUrl("?lat=2&test=4").isValid());
        assertTrue(new MapUrl("?lat=2&lon=4").isValid());
    }
}
