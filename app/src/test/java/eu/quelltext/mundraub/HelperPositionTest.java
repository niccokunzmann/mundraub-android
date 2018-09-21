package eu.quelltext.mundraub;

import org.junit.Test;

import eu.quelltext.mundraub.common.Helper;

import static org.junit.Assert.assertEquals;

public class HelperPositionTest {

    @Test
    public void testGetDirectionNorth() {
        assertDirectionMatches(0, 0, 0, 1, R.string.direction_north);
    }

    @Test
    public void testGetDirectionNorthEast() {
        assertDirectionMatches(0, 0, 1, 1, R.string.direction_north_east);
    }

    @Test
    public void testGetDirectionClose() {
        assertDirectionMatches(0, 0, 0, 0, R.string.direction_too_close);
    }

    @Test
    public void testGetDirectionEast() {
        assertDirectionMatches(0, 0, 1, 0, R.string.direction_east);
    }

    @Test
    public void testGetDirectionSouthEast() {
        assertDirectionMatches(10, 10, 20, 1, R.string.direction_south_east);
    }

    @Test
    public void testGetDirectionSouth() {
        assertDirectionMatches(.5, .5, .5, -1, R.string.direction_south);
    }

    @Test
    public void testGetDirectionSouthWest() {
        assertDirectionMatches(10, 10, 0, 1, R.string.direction_south_west);
    }

    @Test
    public void testGetDirectionWest() {
        assertDirectionMatches(100, 100, 30, 80, R.string.direction_west);
    }

    @Test
    public void testGetDirectionNorthWest() {
        assertDirectionMatches(0, 0, -1, 1, R.string.direction_north_west);
    }
    
    private static String mapToString(int resourceId) {
        switch (resourceId) {
            case R.string.direction_north: return "N";
            case R.string.direction_north_east: return "NE";
            case R.string.direction_east: return "E";
            case R.string.direction_south_east: return "SE";
            case R.string.direction_south: return "S";
            case R.string.direction_south_west: return "SW";
            case R.string.direction_west: return "W";
            case R.string.direction_north_west: return "NW";
        }
        return "??";
    }

    private void assertDirectionMatches(double i, double i1, double i2, double i3, int expectedDirection) {
        int direction = Helper.directionFromPositionToPositionAsResourceId(i, i1, i2, i3);
        assertEquals(mapToString(expectedDirection) + " == " + mapToString(direction), expectedDirection, direction);
    }

}
