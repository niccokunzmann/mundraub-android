package eu.quelltext.mundraub;

import org.junit.Test;

import eu.quelltext.mundraub.map.MapUrl;
import eu.quelltext.mundraub.map.position.BoundingBox;
import eu.quelltext.mundraub.map.position.BoundingBoxCollection;

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
        assertFalse(new MapUrl("?centerLon=2&test=4").isValid());
        assertFalse(new MapUrl("?centerLat=2&test=4").isValid());
        assertTrue(new MapUrl("?centerLat=2&centerLon=4").isValid());
    }

    @Test
    public void testMapUrlWithoutParameterIsInvalid() {
        assertFalse(new MapUrl("").isValid());
    }

    @Test
    public void testMatUrlUsesAppGPSAsDefault() {
        assertEquals("false", new MapUrl("").getString("browserGPS"));
        assertEquals("false", new MapUrl(0, 0).getString("browserGPS"));
    }

    @Test
    public void testMapUrlDoesNotRedirectTilesByDefault() {
        MapUrl mapUrl = new MapUrl("?test=2#test=4");
        assertEquals(null, mapUrl.getString("mapnikUrl"));
        assertEquals(null, mapUrl.getString("earthUrl"));
    }

    @Test
    public void testMapUrlDoesRedirectToLocalhost() {
        for (int port = 4; port < 2000; port += 300) {
            MapUrl mapUrl = new MapUrl("?test=2#test=4").serveTilesFromLocalhost(port);
            assertEquals(
                    "http://localhost:" + port + "/tiles/ArcGIS/${z}/${y}/${x}",
                    mapUrl.getString("earthUrl"));
            assertEquals(
                    "http://localhost:" + port + "/tiles/osm/${z}/${y}/${x}",
                    mapUrl.getString("mapnikUrl"));
        }
    }

    @Test
    public void testMapUrlCanIncludeBoundingBox() {
        BoundingBoxCollection bboxes = BoundingBoxCollection.with(BoundingBox.fromNESW(1,2, 3, 4));
        MapUrl mapUrl = new MapUrl("?").setOfflineAreaBoundingBoxes(bboxes);
        assertEquals(bboxes, mapUrl.getOfflineAreaBoundingBoxes());
    }

    @Test
    public void testMapUrlIncludesBoundingBoxInUrl() {
        BoundingBoxCollection bboxes = BoundingBoxCollection.with(BoundingBox.fromNESW(1,2, 3, 4), BoundingBox.fromNESW(21,23, 344, 24));
        MapUrl mapUrl = new MapUrl(new MapUrl("?").setOfflineAreaBoundingBoxes(bboxes).getUrl());
        BoundingBoxCollection other = mapUrl.getOfflineAreaBoundingBoxes();
        assertEquals(bboxes, other);
    }

    @Test
    public void testMapUrlHasNoBoundingBoxByDefault() {
        MapUrl mapUrl = new MapUrl("?");
        assertEquals(0, mapUrl.getOfflineAreaBoundingBoxes().size());
    }

    @Test
    public void testMarkerPositionIsPositionFromConstructor() {
        MapUrl mapUrl = new MapUrl(22, 33);
        assertEquals(mapUrl.getDouble("markerLon"), 22, 0.1);
        assertEquals(mapUrl.getDouble("markerLat"), 33, 0.1);
    }

    @Test
    public void testCanGetDoubleFromUnsetValue() {
        MapUrl mapUrl = new MapUrl("?test=asd");
        assertTrue(Double.isNaN(mapUrl.getDouble("test")));
        assertTrue(Double.isNaN(mapUrl.getDouble("test1")));
    }

}
