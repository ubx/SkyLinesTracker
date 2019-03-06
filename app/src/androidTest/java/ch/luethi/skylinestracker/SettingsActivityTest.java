package ch.luethi.skylinestracker;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest


public class SettingsActivityTest {

    @Rule
    public ActivityTestRule<SettingsActivity> rule = new ActivityTestRule<>(SettingsActivity.class);

    @Test
    public void aTestX() throws Exception {
        onView(withText(R.string.pref_tracking_key)).check(matches(isDisplayed()));

    }

    @Test
    public void aTestY() throws Exception {
        // todo -- see: https://stackoverflow.com/questions/45172505/testing-android-preferencefragment-with-espresso
        String stringToBetyped = "AAA";
        ViewInteraction ia = onView(withText(R.string.pref_tracking_key));
        ia.perform(typeText(stringToBetyped), closeSoftKeyboard());
        onView(withText(R.string.pref_tracking_key)).perform(click());

        // Check that the text was changed.
        onView(withId(R.string.pref_tracking_key)).check(matches(withText(stringToBetyped)));
    }



}
