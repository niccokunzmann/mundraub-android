package eu.quelltext.mundraub;

import com.google.common.io.Files;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
    @Test
    public void testCompare(){
        assertEquals(Helper.compare(0,1), -1);
        assertEquals(Helper.compare(1,0), 1);
        assertEquals(Helper.compare(0,0), 0);
    }

    @Test
    public void testDistanceInMeters(){
        assertEquals(Helper.distanceInMetersBetween(0.0,0.0,1.0,1.0), 157955.13999241014, 0);
        assertEquals(Helper.distanceInMetersBetween(45.58885,12.34521,12.47785,1.12345), 3874247.1015959415, 0);
    }

    @Test
    public void testMetersToDegrees(){
        assertEquals(Helper.metersToDegrees(123.25),0.0011034613797355577, 0);
    }


    @Test
    public void testDoubleToString(){
        assertEquals(Helper.doubleTo15DigitString(25.25), "25.250000000000000");
        assertEquals(Helper.doubleTo15DigitString(-154782.24445), "-154782.244450000000000");
    }

    @Test
    public void testFolderSize() throws IOException {
        File dir = Files.createTempDir();
        assertEquals(Helper.folderSize(dir), 0);
        // write to a file
        // see https://stackoverflow.com/a/2885224
        byte data[] = new byte[100];
        FileOutputStream out = new FileOutputStream(dir.toString() + "/test.txt");
        out.write(data);
        out.close();
        long size = Helper.folderSize(dir);
        assertEquals(size, 100);
    }
}
