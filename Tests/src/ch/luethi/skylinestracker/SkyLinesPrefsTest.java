package ch.luethi.skylinestracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = 18)

public class SkyLinesPrefsTest {

    SkyLinesPrefs pref;

    @Before
    public void setUp() {
        pref = new SkyLinesPrefs(Robolectric.application.getApplicationContext());
    }


    @Test
    public void testKeyStore() {
        pref.setTrackingKey("123ABCD");
        long key = pref.getTrackingKey();
        assertThat("Key not correct stored", Long.toHexString(key).toUpperCase(), equalTo("123ABCD"));
    }

    @Test
    public void testDefAutoStartTracking() {
        assertThat("Wrong default Auto Start Tracking", pref.isAutostartTracking(), equalTo(false));
    }

    @Test
    public void testSetAutoStartTracking() {
        pref.setAutostartTracking(true);
        assertThat("Wrong set Auto Start Tracking", pref.isAutostartTracking(), equalTo(true));
        pref.setAutostartTracking(false);
        assertThat("Wrong set Auto Start Tracking", pref.isAutostartTracking(), equalTo(false));
        pref.setAutostartTracking(true);
        assertThat("Wrong set Auto Start Tracking", pref.isAutostartTracking(), equalTo(true));
    }

    @Test
    public void testDefSmsConfig() {
        assertThat("Wrong default SMS Config", pref.isSmsConfig(), equalTo(false));
    }

    @Test
    public void testSmsConfig() {
        pref.setSmsConfig(true);
        assertThat("Wrong set SMS Config", pref.isSmsConfig(), equalTo(true));
        pref.setSmsConfig(false);
        assertThat("Wrong set SMS Config", pref.isSmsConfig(), equalTo(false));
        pref.setSmsConfig(true);
        assertThat("Wrong set SMS Config", pref.isSmsConfig(), equalTo(true));
    }

    @Test
    public void testDefIpAddress() {
        String ipAddress = pref.getIpAddress();
        assertThat("Wrong default ip address", ipAddress, equalTo("95.128.34.172"));
    }

    @Test
    public void testIssue_1() {
        pref.setTrackingKey("NOHEX");
        long key = pref.getTrackingKey();
        assertThat("None hex key shout return 0 long value", 0, equalTo(0));
    }


}
