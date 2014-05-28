package ch.luethi.skylinestracker;

import android.content.Intent;
import android.widget.CheckBox;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.robolectric.Robolectric.application;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk=18)

public class MainActivityTest {

    MainActivity mainActivity;

    @Before
    public void setUp() {
        Intent intent = new Intent(application, MainActivity.class);
        intent.putExtra(MainActivity.ISTESTING, true);
        mainActivity = buildActivity(MainActivity.class).withIntent(intent).create().get();
    }

    @Test
    public void testCorrectAppName() {
        assertThat("Wrong app name", mainActivity.getResources().getString(R.string.app_name), equalTo("SkyLines Tracker"));
    }

    @Test
    public void testDefaultValue() {
        CheckBox cb = (CheckBox) mainActivity.findViewById(R.id.checkLiveTracking);
        assertThat("Checkbox should be unchecked", cb.isChecked(), equalTo(false));
    }


}
