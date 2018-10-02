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
}
