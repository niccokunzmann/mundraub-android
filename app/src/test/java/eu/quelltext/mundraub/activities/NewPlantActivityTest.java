package eu.quelltext.mundraub.activities;

import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import eu.quelltext.mundraub.R;
import eu.quelltext.mundraub.plant.Plant;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class NewPlantActivityTest {

    private NewPlantActivity activity;

    @Before
    public void setUp() {
        // Initialize NewPlantActivity activity
        activity = Robolectric.setupActivity(NewPlantActivity.class);
    }

    @Test
    public void checkActivityNotNull() {
        assertNotNull(activity);
    }

    @Test
    public void saveButtonClickShouldRemoveNewPlant() {
        Plant plant = activity.getPlant();
        Button button = (Button) activity.findViewById(R.id.button_save);
        button.performClick();
        assertFalse(plant.exists());
    }

    @Test
    public void saveButtonClickShouldNotRemoveNewPlant() {
        Plant plant = activity.getPlant();
        plant.setDescription("Plant description");
        Button button = (Button) activity.findViewById(R.id.button_save);
        button.performClick();
        assertTrue(plant.exists());
    }
}