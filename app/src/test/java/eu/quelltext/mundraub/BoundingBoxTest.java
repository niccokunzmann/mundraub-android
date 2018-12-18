package eu.quelltext.mundraub;

import org.json.JSONException;
import org.junit.Test;

import eu.quelltext.mundraub.map.position.BoundingBox;
import eu.quelltext.mundraub.map.position.Position;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class BoundingBoxTest {

    @Test
    public void testPositionsMovingWithBoundingBoxAreAlwaysInside() {
        for (int i = -1000; i < 700; i++) {
            BoundingBox bbox = BoundingBox.fromNESW(1, i + 1, -1, i - 1);
            assertTrue(bbox.contains(new Position(i, 0)));
            assertTrue(bbox.contains(new Position(i + 360, 0)));
            assertTrue(bbox.contains(new Position(i - 360, 0)));
            assertFalse(bbox.contains(new Position(i, 2)));
            assertFalse(bbox.contains(new Position(i, -2)));
            assertFalse(bbox.contains(new Position(i + 2, 0)));
            assertFalse(bbox.contains(new Position(i - 2, 0)));
        }
    }

    @Test
    public void testMiddle() {
        assertEquals(new Position(0, 0), BoundingBox.fromNESW(1, 1, -1, -1).middle());
        assertEquals(new Position(3, 3), BoundingBox.fromNESW(7, 4, -1, 2).middle());
    }

    @Test
    public void testEquality() {
        assertEquals(BoundingBox.fromNESW(1, 2,3, 4), BoundingBox.fromNESW(1, 2,3, 4));
        assertEquals(BoundingBox.fromNESW(3, 44,32, 41).hashCode(), BoundingBox.fromNESW(3, 44,32, 41).hashCode());
    }

    @Test
    public void testInequality() {
        assertFalse(BoundingBox.fromNESW(11, 2,3, 4).equals(BoundingBox.fromNESW(1, 2,3, 4)));
        assertFalse(BoundingBox.fromNESW(1, 21,3, 4).equals(BoundingBox.fromNESW(1, 2,3, 4)));
        assertFalse(BoundingBox.fromNESW(1, 2,31, 4).equals(BoundingBox.fromNESW(1, 2,3, 4)));
        assertFalse(BoundingBox.fromNESW(1, 2,3, 41).equals(BoundingBox.fromNESW(1, 2,3, 4)));
    }

    @Test
    public void testJSON() throws JSONException {
        for (int i = 0; i < 100; i += 30) {
            BoundingBox bbox = BoundingBox.fromNESW(1, i, 3 + i, 4 * i);
            assertEquals(bbox, BoundingBox.fromJSON(bbox.toJSON()));
        }
    }

    @Test
    public void testAround180Lon1() {
        BoundingBox bbox = BoundingBox.fromNESW(1, -170, 0, 170);
        assertEquals(20., bbox.deltaLongitude(), 0.001);
        assertTrue(bbox.crosses180());
    }

    @Test
    public void testAround180Lon2() {
        BoundingBox bbox = BoundingBox.fromNESW(10, -179, -30, 178);
        assertEquals(3., bbox.deltaLongitude(), 0.001);
        assertTrue(bbox.crosses180());
    }

    @Test
    public void testAround180Lon3() {
        BoundingBox bbox = BoundingBox.fromNESW(10, 179, -30, -178);
        assertEquals(357, bbox.deltaLongitude(), 0.001);
        assertFalse(bbox.crosses180());
    }
    
        @Test
    public void testSQLQueryString(){
        BoundingBox bbox = BoundingBox.fromNESW(10, 179, -30, -178);
        assertEquals(" ( 1 < 179.0 and 1 > -178.0 )  and  ( 1 > -30.0 and 1 < 10.0)", bbox.asSqlQueryString("1", "1"));
        BoundingBox bbox2 = BoundingBox.fromNESW(10, -179, -30, 178);
        assertEquals(" ( 1 < -179.0 or 1 > 178.0 )  and  ( 1 > -30.0 and 1 < 10.0)", bbox2.asSqlQueryString("1", "1"));
    }

    @Test
    public void testDistanceBetweenPositions(){
        BoundingBox bbox = BoundingBox.fromNESW(10, 179, -30, -178);
        assertEquals(1116939.86090874, bbox.distanceInMetersBetween(new Position(0, 0), new Position(0, 10)));
    }

    @Test
    public void testFromWestSouthEastNorthArray(){
        String[]  strings = {"10.4", "12", "50", "-20.1"};
        BoundingBox bbox = BoundingBox.fromWestSouthEastNorthArray(strings);
        assertEquals(-20.1, bbox.northBorderLatitude());
        assertEquals(12.0, bbox.southBorderLatitude());
    }

    @Test
    public void testRadiusContains(){
        Position position = new Position(0, 0);
        BoundingBox radiusBoundingBox = RadiusBoundingBox.fromPositionAndRadius(position, 20.0);
        assertEquals(true, radiusBoundingBox.contains(new Position(0, 0)));
        assertEquals(false, radiusBoundingBox.contains(new Position(30, 20)));
    }


    @Test
    public void testRadiusSQLQueryString(){
        Position position = new Position(20, 22);
        BoundingBox radiusBoundingBox = RadiusBoundingBox.fromPositionAndRadius(position, 20.0);
        assertEquals(" ( 1 < 20.00017906067015 and 1 > 19.99982093932985 )  and  ( 1 > 21.99982093932986 and 1 < 22.00017906067014)", radiusBoundingBox.asSqlQueryString("1", "1"));
        Position position2 = new Position(-20, 22);
        BoundingBox radiusBoundingBox2 = RadiusBoundingBox.fromPositionAndRadius(position, 20.0);
        assertEquals(" ( 1 < 20.00017906067015 and 1 > 19.99982093932985 )  and  ( 1 > 21.99982093932986 and 1 < 22.00017906067014)", radiusBoundingBox2.asSqlQueryString("1", "1"));

    }


    @Test
    public void testRadiusLongitude(){
        Position position = new Position(0, 2);
        BoundingBox radiusBoundingBox = RadiusBoundingBox.fromPositionAndRadius(position, 20.0);
        assertEquals(2.0, radiusBoundingBox.middle().getLatitude());
    }

    @Test
    public void testRadiusLatitude(){
        Position position = new Position(0, 2);
        BoundingBox radiusBoundingBox = RadiusBoundingBox.fromPositionAndRadius(position, 20.0);
        assertEquals(0.0, radiusBoundingBox.middle().getLongitude());
    }
}
