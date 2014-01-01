package ch.luethi.skylinestracker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
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
    public void testIssue_1() {
        pref.setTrackingKey("NOHEX");
        long key = pref.getTrackingKey();
        assertThat("None hex key shout return 0 long value", 0, equalTo(0));
    }


}
