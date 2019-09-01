package eu.quelltext.mundraub;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import eu.quelltext.mundraub.plant.Plant;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class PlantTest {
    private Plant plainPlant;
    private Plant plantWithCount;
    private Plant plantWithDescription;

    @Before
    public void setUp() {
        // Initialize plain plant
        plainPlant = new Plant();
        // Initialize plant with count
        plantWithCount = new Plant();
        plantWithCount.setCount(1);
        // Initialize plant with description
        plantWithDescription = new Plant();
        plantWithDescription.setDescription("some description");
    }

    @Test
    public void testPlant_isDefault_returnTrue() {
        assertTrue(plainPlant.isDefault());
    }

    @Test
    public void testPlant_notDefault_returnFalse() {
        assertFalse(plantWithCount.isDefault());
        assertFalse(plantWithDescription.isDefault());
    }

    @Test
    public void testPlant_save_returnTrue() {
        plainPlant.save();
        assertTrue(plainPlant.exists());
        plantWithCount.save();
        assertTrue(plantWithCount.exists());
        plantWithDescription.save();
        assertTrue(plantWithDescription.exists());
    }

    @Test
    public void testPlant_delete_returnFalse() {
        plainPlant.delete();
        assertFalse(plainPlant.exists());
        plantWithCount.delete();
        assertFalse(plantWithCount.exists());
        plantWithDescription.delete();
        assertFalse(plantWithDescription.exists());
    }
}