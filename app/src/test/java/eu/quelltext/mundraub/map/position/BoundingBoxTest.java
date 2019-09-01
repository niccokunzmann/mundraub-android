package eu.quelltext.mundraub.map.position;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.*;

public class BoundingBoxTest {

    @Test
    public void fromWSEN() {
        assertEquals(BoundingBox.fromWSEN(1, 2,3, 4), BoundingBox.fromWSEN(1, 2,3, 4));
        assertEquals(BoundingBox.fromWSEN(3, 44,32, 41), BoundingBox.fromWSEN(3, 44,32, 41));

        assertNotEquals(BoundingBox.fromWSEN(11, 2,3, 4), (BoundingBox.fromWSEN(10, 21,93, 4)));
        assertNotEquals(BoundingBox.fromWSEN(1, 21,3, 4), (BoundingBox.fromWSEN(11, 12,43, 14)));
    }


    @Test
    public void getLongitude() {
        double delta = 1e-2;
        double longitude1 = 20.45;
        double longitude2 = -250.00;
        assertEquals(20.45, BoundingBox.getLongitude(longitude1), delta);
        assertEquals(110.00, BoundingBox.getLongitude(longitude2), delta);
    }

    @Test
    public void getLatitude() {
        double delta = 1e-6;
        double latitude1 = -125.00;
        double latitude2 = 158.00;
        double latitude3 = 50.00;
        double latitude4 = -50.00;
        assertEquals(-90.00, BoundingBox.getLatitude(latitude1), delta);
        assertEquals(90.00, BoundingBox.getLatitude(latitude2), delta);
        assertEquals(50.00, BoundingBox.getLatitude(latitude3), delta);
        assertEquals(-50.00, BoundingBox.getLatitude(latitude4), delta);

    }


}