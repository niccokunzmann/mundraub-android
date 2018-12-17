package eu.quelltext.mundraub.activities;

import static org.junit.Assert.*;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNull.notNullValue;
import org.junit.Test;



import eu.quelltext.mundraub.R;

public class CodexActivityTest {

    @Test
    public void ViewSoundResonableLounchedWindow () throws Exception {
        String PlantCategory = "Offline Settings";
        onView(withId(R.id.button_accept)).perform(click());
        onView(withText(PlantCategory)).check(matches(notNullValue()));
    }

}