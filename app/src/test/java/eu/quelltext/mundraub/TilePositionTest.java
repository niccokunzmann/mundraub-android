package eu.quelltext.mundraub;

import org.junit.Test;

import eu.quelltext.mundraub.map.position.TilePosition;

import static junit.framework.Assert.assertEquals;

public class TilePositionTest {

    @Test
    public void testNorthTileHasNoTileInTheNorth() {
        assertEquals(null, new TilePosition(0, 0, 3).oneNorth());
    }

    @Test
    public void testTileInTheNorth() {
        assertEquals(new TilePosition(0, 0, 10), new TilePosition(0, 1, 10).oneNorth());
        assertEquals(new TilePosition(22, 33, 10), new TilePosition(22, 34, 10).oneNorth());
    }

    @Test
    public void testSouthTileHasNoTileInTheSouth() {
        assertEquals(null, new TilePosition(0, (1<<3) - 1, 3).oneSouth());
    }

    @Test
    public void testTileInTheSouth() {
        assertEquals(new TilePosition(0, 2, 3), new TilePosition(0, 1, 3).oneSouth());
        assertEquals(new TilePosition(22, 33, 10), new TilePosition(22, 32, 10).oneSouth());
    }

    @Test
    public void testTileToTheEastOverflow() {
        assertEquals(new TilePosition(0, 2, 3), new TilePosition((1 << 3) - 1, 2, 3).oneEast());
    }

    @Test
    public void testTileToTheEast() {
        assertEquals(new TilePosition(22, 33, 10), new TilePosition(21, 33, 10).oneEast());
    }

    @Test
    public void testTileToTheWestOverflow() {
        assertEquals(new TilePosition((1 << 3) - 1, 2, 3), new TilePosition(0, 2, 3).oneWest());
    }

    @Test
    public void testTileToTheWest() {
        assertEquals(new TilePosition(22, 33, 10), new TilePosition(23, 33, 10).oneWest());
    }
}
