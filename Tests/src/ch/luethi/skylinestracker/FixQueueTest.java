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

    public static final int MAX_DATA = 130;
    public static final int REMOVE_ELEMENT = MAX_DATA / 3;
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
        for (int i = 0; i < MAX_DATA; i++) {
            byte[] pl = payload.clone();
            pl[0] = (byte) i;
            fixQueue.push(pl);
        }
        assertThat("Wrong number of elements in queue", fixQueue.size(), equalTo(MAX_DATA));
    }

    @Test
    public void testPop() throws Exception {
        testPush();
        verifyQueue();
        assertThat("Queue not empty", fixQueue.size(), equalTo(0));
    }

    @Test
    public void testRemoveElementAt() throws Exception {
        testPush();
        fixQueue.removeElementAt(REMOVE_ELEMENT);
        assertThat("Wrong number of elements in queue", fixQueue.size(), equalTo(MAX_DATA - 1));
        fixQueue.removeElementAt(0);
        assertThat("Wrong number of elements in queue", fixQueue.size(), equalTo(MAX_DATA - 2));
        fixQueue.removeElementAt(fixQueue.size() - 1);
        assertThat("Wrong number of elements in queue", fixQueue.size(), equalTo(MAX_DATA - 3));
    }


    @Test
    public void testLoad() throws Exception {
        testPush();
        fixQueue = new FixQueue<byte[]>(RuntimeEnvironment.application.getApplicationContext()).load();
        assertThat("Wrong number of elements in queue", fixQueue.size(), equalTo(MAX_DATA));
        verifyQueue();
    }

    @Test
    public void testPushPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < MAX_DATA; i++) {
            byte[] pl = payload.clone();
            pl[0] = (byte) i;
            fixQueue.push(pl);
        }
        long estimatedTime = System.currentTimeMillis() - startTime;
        printElapsed("Measure Push", estimatedTime);
    }

    @Test
    public void testLoadPerformance() throws Exception {
        testPush();
        long startTime = System.currentTimeMillis();
        fixQueue = new FixQueue<byte[]>(RuntimeEnvironment.application.getApplicationContext()).load();
        long estimatedTime = System.currentTimeMillis() - startTime;
        printElapsed("Measure Load", estimatedTime);
        verifyQueue();
    }

    private void printElapsed(String testName, long estimatedTime) {
        System.out.println(testName + ": elapsed=" + estimatedTime + "ms");
    }


    private void verifyQueue() {
        for (int l = fixQueue.size() - 1; l >= 0; --l) {
            byte[] plr = fixQueue.pop();
            assertThat("Wrong data poped", plr[0], equalTo((byte)l));
        }
    }
}