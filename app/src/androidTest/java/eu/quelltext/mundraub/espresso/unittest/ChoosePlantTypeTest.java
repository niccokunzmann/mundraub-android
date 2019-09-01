package eu.quelltext.mundraub.espresso.unittest;

import org.junit.Test;

import eu.quelltext.mundraub.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNull.notNullValue;


public class ChoosePlantTypeTest {
    @Test
    public void ViewPlantCategoryLounchedWindow () throws Exception {
        String PlantCategory = "Plant Category";
        onView(withId(R.id.button_choose_plant_type)).perform(click());
        onView(withText(PlantCategory)).check(matches(notNullValue()));

    }

    @Test
    public void ViewDeleteLounchedWindow () throws Exception {
        String Delete = "Plant Deleted";
        onView(withId(R.id.button_cancel)).perform(click());
        onView(withText(Delete)).check(matches(notNullValue()));

    }
}