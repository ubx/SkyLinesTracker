package ch.luethi.skylinestracker;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.uiautomator.UiDevice;
import ch.luethi.skylinestracker.utils.UiAutomatorUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static ch.luethi.skylinestracker.utils.UiAutomatorUtils.*;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@LargeTest


public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    private UiDevice device;

    @Before
    public void setUp() {
        this.device = UiDevice.getInstance(getInstrumentation());
    }


    @Test
    public void aTestShouldDisplayPermissionRequestDialogAtStartup() throws Exception {
        assertViewWithTextIsVisible(device, UiAutomatorUtils.TEXT_ALLOW);
        assertViewWithTextIsVisible(device, UiAutomatorUtils.TEXT_DENY);

        // cleanup for the next test
        denyCurrentPermission(device);
    }

//    @Test
//    public void bTestShouldDisplayShortRationaleIfPermissionWasDenied() throws Exception {
//        denyCurrentPermission(device);
//
//        onView(withText(R.string.permission_denied_rationale_short)).check(matches(isDisplayed()));
//        onView(withText(R.string.grant_permission)).check(matches(isDisplayed()));
//    }

    @Test
    public void cTestStart() throws Exception {
        allowCurrentPermission(device);
        onView(withId(R.id.checkLiveTracking)).perform(click());
    }

    @Test
    public void dTestSettings() throws Exception {
        allowCurrentPermission(device);
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.action_settings)).perform(click());
    }

    @Test
    public void eTestAbout() throws Exception {
        allowCurrentPermission(device);
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.action_about)).perform(click());
    }

    @Test
    public void fTestExit() throws Exception {
        allowCurrentPermission(device);
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(R.string.action_exit)).perform(click());
    }


}
