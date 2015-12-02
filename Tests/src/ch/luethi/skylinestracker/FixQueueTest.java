package ch.luethi.skylinestracker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)

public class FixQueueTest {

    public static final int MAX_DATA = 100;
    FixQueue<byte[]> fixQueue;
    byte[] payload;

    @Before
    public void setUp() throws Exception {
        fixQueue = new FixQueue<byte[]>(RuntimeEnvironment.application.getApplicationContext()).load();
        payload = new byte[48];
        for (byte i = 1; i < payload.length; i++) {
            payload[i] = i;
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testPush() throws Exception {
        for (byte i = 0; i < MAX_DATA; i++) {
            byte[]  pl = payload.clone();
            pl[0] = i;
            fixQueue.push(pl);
        }
        assertThat("Wrong number of elements in queue", fixQueue.size(), equalTo(MAX_DATA));
    }

    @Test
    public void testPop() throws Exception {
        testPush();
        for (int l = fixQueue.size() - 1; l >= 0; --l) {
            byte[] plr = fixQueue.pop();
            assertThat("Wrong data poped", Integer.valueOf(plr[0]), equalTo(l));
        }
    }

    @Test
    public void testRemoveElementAt() throws Exception {

    }

    @Test
    public void testSize() throws Exception {

    }

    @Test
    public void testIsEmpty() throws Exception {

    }

    @Test
    public void testLoad() throws Exception {

    }
}