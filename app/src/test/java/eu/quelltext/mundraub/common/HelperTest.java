package eu.quelltext.mundraub.common;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HelperTest {

    @Test
    public void doubleTo15DigitString() {
        double number1 = 5.123456789123456789;
        String dString1 = Helper.doubleTo15DigitString(number1);

        double doubleResult1 = 5.123456789123456;
        String stringResult1 = Double.toString(doubleResult1);

        assertEquals(stringResult1, dString1);

        double number2 = 12.9856332145624323233;                ;
        String dString2 = Helper.doubleTo15DigitString(number2);

        double doubleResult2 = 12.985633214562432;
        String stringResult2 = Double.toString(doubleResult2);

        assertEquals(stringResult2, dString2);
    }

    @Test
    public void metersToDegrees() {
        double delta = 1e-6;
        double meters = 1000;
        double result = 0.008953;
        double degrees = Helper.metersToDegrees(meters);

        assertEquals(result, degrees, delta);
    }

    @Test
    public void distanceInMetersBetween() {
        double delta = 1e-6;
        double longitude1 = 10.01;
        double latitude1 = 11.02;
        double longitude2 = 12.03;
        double latitude2 = 13.04;

        double distanceInMetersBetween = Helper.distanceInMetersBetween(longitude1, latitude1, longitude2, latitude2);
        double expected = 315583.811651;

        assertEquals(expected, distanceInMetersBetween, delta);
    }

    @Test
    public void compare() {
        long a = 5;
        long b = 4;
        assertTrue(a > b);
        assertFalse(a < b);
    }

    @Test
    public void deleteDir() {
        try{

            //create a temp file
            File temp = File.createTempFile("temporary", ".txt");
            assertTrue(temp.exists());
            Helper.deleteDir(temp);
            assertFalse(temp.exists());

        }catch(IOException e){

            e.printStackTrace();
        }

    }
}
