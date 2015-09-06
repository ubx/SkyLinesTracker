package ch.luethi.skylinestracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)

public class SkyLinesPrefsTest {

    SkyLinesPrefs pref;

    @Before
    public void setUp() {
        pref = new SkyLinesPrefs(RuntimeEnvironment.application.getApplicationContext());
    }


    @Test
    public void testKeyStore() {
        pref.setTrackingKey("123ABCD");
        long key = pref.getTrackingKey();
        assertThat("Key not correct stored", Long.toHexString(key).toUpperCase(), equalTo("123ABCD"));
    }

    @Test
    public void testDefAutoStartTracking() {
        assertThat("Wrong default Auto Start Tracking", pref.isAutoStartTracking(), equalTo(false));
    }

    @Test
    public void testSetAutoStartTracking() {
        pref.setAutoStartTracking(true);
        assertThat("Wrong set Auto Start Tracking", pref.isAutoStartTracking(), equalTo(true));
        pref.setAutoStartTracking(false);
        assertThat("Wrong set Auto Start Tracking", pref.isAutoStartTracking(), equalTo(false));
        pref.setAutoStartTracking(true);
        assertThat("Wrong set Auto Start Tracking", pref.isAutoStartTracking(), equalTo(true));
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
        String ipAddress = pref.getIpAddress("1.2.3.4");
        assertThat("Wrong default ip address", ipAddress, equalTo("1.2.3.4"));
    }

    @Test
    public void testDefIpAddressDef() {
        String ipAddress = pref.getIpAddress();
        assertThat("Wrong default ip address", ipAddress, equalTo(""));
    }

    @Test
    public void testIssue_1() {
        pref.setTrackingKey("NOHEX");
        long key = pref.getTrackingKey();
        assertThat("None hex key shout return 0 long value", 0, equalTo(0));
    }

    @Test
    public void testIssue_7() {
        pref.setIpAddress("1.2.3.4")  ;
        assertThat("Wrong default ip address",  pref.getIpAddress(), equalTo("1.2.3.4"));
        pref.setIpAddress("95.128.34.172")  ;
        assertThat("Wrong default ip address",  pref.getIpAddress(), equalTo("95.128.34.172"));
    }


}
