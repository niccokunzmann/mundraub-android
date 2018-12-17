package eu.quelltext.mundraub.espresso.unittest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNull.notNullValue;
import org.junit.Test;
import eu.quelltext.mundraub.R;

public class SettingActivityTest {

    @Test
    public void DownloadMapLounched () throws Exception {

        String PopUpWindow1 = "All selected parts of the map have been downloaded and are now available offline";

        onView(withId(R.id.button_start_map_download)).perform(click());
        onView(withText(PopUpWindow1)).check(matches(notNullValue()));
    }

    @Test
    public void RemoveAllLounched () throws Exception {

        String PopUpWindow2 = "You deleted all offline areas but the downloaded parts are still using up space. Would you like to delete all affline titles?";

        onView(withId(R.id.button_remove_areas)).perform(click());
        onView(withText(PopUpWindow2)).check(matches(notNullValue()));
    }
}