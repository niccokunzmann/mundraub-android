package eu.quelltext.mundraub;

import org.junit.Test;

import eu.quelltext.mundraub.map.position.BoundingBox;
import eu.quelltext.mundraub.map.position.Position;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class BoundingBoxInclusionTest {

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
}
