package ch.luethi.skylinestracker;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

/**
 * Created by andreas on 02.08.16.
 */
public class IntegrationTest {

    private static final String TESTS_SCRIPTS = "Tests/scripts/";
    Set<Rec> recsRcv = new HashSet<Rec>(200);
    Set<Rec> recsSim = new HashSet<Rec>(200);

    private class Rec {

        public Rec(boolean ignorSecDay) {
            this.ignorSecDay = ignorSecDay;
        }

        int secDay;
        String key;
        double lat, log;
        boolean ignorSecDay;

        @Override
        public boolean equals(Object o) {
            Rec r = (Rec) o;
            return (ignorSecDay || secDay == r.secDay) & key.equals(r.key)
                    & Math.abs(lat - r.lat) < 0.00002
                    & Math.abs(log - r.log) < 0.00002;
        }
    }


    private void readOutFile(String fileName, Set<Rec> recs, String prefix, int tOffset, boolean ignorSecDay) {
        Path path = Paths.get(fileName);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(l -> {
                if (l.startsWith(prefix)) {
                    String str = l.substring(prefix.length());
                    Rec r = new Rec(ignorSecDay);
                    String[] rs = str.split(",");
                    if (rs.length == 5) {
                        r.secDay = Integer.decode(rs[0]) + tOffset;
                        r.key = rs[1];
                        r.lat = Double.parseDouble(rs[2]);
                        r.log = Double.parseDouble(rs[3]);
                        recs.add(r);
                    }
                }
            });
        } catch (IOException ex) {

        }
    }

    private void readOutFile(String fileName, Set<Rec> recs, String prefix, int tOffset) {
        readOutFile(fileName, recs, prefix, tOffset, false);

    }


    private boolean containsAll(Set<Rec> s1, Set<Rec> s2) {
        boolean df = false;
        for (Rec r1 : s1) {
            Rec found = null;
            int foundCnt = 0;
            for (Rec r2 : s2) {
                if (r1.equals(r2)) {
                    found = r2;
                    foundCnt++;
                }
            }
            if (found != null) {
                s2.remove(found);
                if (foundCnt > 1) {
                    System.out.println("foundCnt=" + foundCnt);
                    df = true;
                }
            }
        }
        System.out.println("diff=" + s2.size());
        for (Rec r2 : s2) {
            System.out.println("diff Rec: " + r2.secDay + " / " + r2.lat + " / " + r2.log);
        }
        return s2.size() <= 2 & !df;
    }


    private void runScript(String... scriptFileAndParams) {
        ProcessBuilder pb = new ProcessBuilder(scriptFileAndParams);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        try {
            Process p = pb.start();
            System.out.println("executing script " + scriptFileAndParams[0] + " ...");
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            TimeUnit.SECONDS.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void printRecsSize() {
        System.out.println("recsSim=" + recsSim.size());
        System.out.println("recsRcv=" + recsRcv.size());
    }

    @Before
    public void setUp() {
        //String env = System.getenv("testwith");
        recsSim.clear();
        recsRcv.clear();
    }


    @Test
    public void testBasic() {
        runScript(TESTS_SCRIPTS + "integrationTest-basic.sh");

        readOutFile(TESTS_SCRIPTS + "rcv-test-00.out", recsRcv, "Rcv: ", 0);
        readOutFile(TESTS_SCRIPTS + "sim-test.out", recsSim, "Sim: ", 0);
        printRecsSize();
        assertTrue("Rcv shout nothing receive", recsRcv.size() == 0);

        recsRcv.clear();
        readOutFile(TESTS_SCRIPTS + "rcv-test-01.out", recsRcv, "Rcv: ", 0);
        System.out.println("recsRcv=" + recsRcv.size());
        assertTrue("Sims not big enough...", recsSim.size() >= recsRcv.size());
        assertTrue("Rcv not in Sim", containsAll(recsSim, recsRcv));

        recsRcv.clear();
        readOutFile(TESTS_SCRIPTS + "rcv-test-02.out", recsRcv, "Rcv: ", 0);
        System.out.println("recsRcv=" + recsRcv.size());
        assertTrue("Rcv shout nothing receive", recsRcv.size() == 0);
    }

    @Test
    public void testBasicRealHW() {
        runScript(TESTS_SCRIPTS + "integrationTest-basic-real-HW.sh");

        readOutFile(TESTS_SCRIPTS + "rcv-test-00.out", recsRcv, "Rcv: ", 0);
        readOutFile(TESTS_SCRIPTS + "sim-test.out", recsSim, "Sim: ", 0, true);
        printRecsSize();

        // todo -- assertTrue(... recsRcv.size() > 0);
        assertTrue("Rcv shout nothing receive", recsRcv.size() == 0);

        recsRcv.clear();
        readOutFile(TESTS_SCRIPTS + "rcv-test-01.out", recsRcv, "Rcv: ", 0, true);
        System.out.println("recsRcv=" + recsRcv.size());
        assertTrue("Sims not big enough...", recsSim.size() >= recsRcv.size());
        assertTrue("Rcv not in Sim", containsAll(recsSim, recsRcv));

        recsRcv.clear();
        readOutFile(TESTS_SCRIPTS + "rcv-test-02.out", recsRcv, "Rcv: ", 0);
        System.out.println("recsRcv=" + recsRcv.size());
        assertTrue("Rcv shout nothing receive", recsRcv.size() == 0);
    }

    @Test
    public void testStartWithDisconnected() {
        runScript(TESTS_SCRIPTS + "integrationTest-start-with-disconnected.sh");

        readOutFile(TESTS_SCRIPTS + "rcv-test.out", recsRcv, "Rcv: ", 0);
        readOutFile(TESTS_SCRIPTS + "sim-test.out", recsSim, "Sim: ", 0);

        printRecsSize();

        assertTrue("Sims not big enough...", recsSim.size() >= recsRcv.size());
        assertTrue("Rcv not in Sim", containsAll(recsSim, recsRcv));
    }

    @Test
    public void testStartWithDisconnectedHW() {
        runScript(TESTS_SCRIPTS + "integrationTest-start-with-disconnected-HW.sh");

        readOutFile(TESTS_SCRIPTS + "rcv-test.out", recsRcv, "Rcv: ", 0, true);
        readOutFile(TESTS_SCRIPTS + "sim-test.out", recsSim, "Sim: ", 0, true);

        printRecsSize();

        //todo -- assertTrue("Sims not big enough...", recsSim.size() >= recsRcv.size());
        assertTrue("Rcv not in Sim", containsAll(recsSim, recsRcv));
    }

    @Test
    public void testQueue() {
        runScript(TESTS_SCRIPTS + "integrationTest-queue.sh");

        readOutFile(TESTS_SCRIPTS + "rcv-test.out", recsRcv, "Rcv: ", 0);
        readOutFile(TESTS_SCRIPTS + "sim-test.out", recsSim, "Sim: ", 0);

        printRecsSize();

        assertTrue("Sims not big enough...", recsSim.size() >= recsRcv.size());
        assertTrue("Rcv not in Sim", containsAll(recsSim, recsRcv));
    }

    @Test
    public void testStartLostFixes() {
        runScript(TESTS_SCRIPTS + "integrationTest-lost-fixes.sh");

        readOutFile(TESTS_SCRIPTS + "rcv-test.out", recsRcv, "Rcv: ", 0);
        readOutFile(TESTS_SCRIPTS + "sim-test.out", recsSim, "Sim: ", 0);

        printRecsSize();

        assertTrue("Rcv count not correct", recsRcv.size() == recsSim.size() - 320); // 80 or 81
        assertTrue("Sims not big enough...", recsSim.size() >= recsRcv.size());
        assertTrue("Rcv not in Sim", containsAll(recsSim, recsRcv));
    }

    @Ignore
    @Test
    public void testBatteryUsageWithoutQueue() {
        runScript(TESTS_SCRIPTS + "integrationTest-battery-usage.sh", "false");
    }

    @Ignore
    @Test
    public void testBatteryUsageWithQueue() {
        runScript(TESTS_SCRIPTS + "integrationTest-battery-usage.sh", "true");
    }

}
