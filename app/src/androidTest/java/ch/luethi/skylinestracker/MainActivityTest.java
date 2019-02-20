package ch.luethi.skylinestracker;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
@SdkSuppress(minSdkVersion = 18)

@LargeTest


public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);



    @Test
    public void testStart() {
        // todo -- click on ALLOW
        //  onView(withText("ALLOW")).perform(click());
        UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());


        onView(withId(R.id.checkLiveTracking)).perform(click());
    }

    @Test
    public void testExit() {
        onView(withId(R.id.action_exit)).perform(click());
    }




}
