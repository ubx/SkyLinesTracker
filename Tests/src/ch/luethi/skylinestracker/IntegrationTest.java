package ch.luethi.skylinestracker;

import org.junit.Before;
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
        int secDay;
        String key;
        double lat, log;

        @Override
        public boolean equals(Object o) {
            Rec r = (Rec) o;
            return secDay == r.secDay & key.equals(r.key)
                     & Math.abs(lat - r.lat) < 0.00002
                     & Math.abs(log - r.log) < 0.00002;
        }
    }


    private void readOutFile(String fileName, Set<Rec> recs, String prefix, int tOffset) {
        Path path = Paths.get(fileName);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(l -> {
                if (l.startsWith(prefix)) {
                    String str = l.substring(prefix.length());
                    //System.out.println(str);
                    Rec r = new Rec();
                    String[] rs = str.split(",");
                    r.secDay = Integer.decode(rs[0]) + tOffset;
                    r.key = rs[1];
                    r.lat = Double.parseDouble(rs[2]);
                    r.log = Double.parseDouble(rs[3]);
                    recs.add(r);
                }
            });
        } catch (IOException ex) {

        }
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


    private void runScript(String scriptFile) {
        ProcessBuilder pb = new ProcessBuilder(scriptFile);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        try {
            Process p = pb.start();
            System.out.println("executing script " + scriptFile + " ...");
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
        recsSim.clear();
        recsRcv.clear();
    }


    @Test
    public void testBasic() {
        runScript(TESTS_SCRIPTS + "integrationTest-basic.sh");

        readOutFile(TESTS_SCRIPTS + "rcv-test-00.out", recsRcv, "Rcv: ", 14400000);  // todo -- why this offset?
        readOutFile(TESTS_SCRIPTS + "sim-test.out", recsSim, "Sim: ", 0);
        printRecsSize();
        assertTrue("Rcv shout nothing receive", recsRcv.size() == 0);

        recsRcv.clear();
        readOutFile(TESTS_SCRIPTS + "rcv-test-01.out", recsRcv, "Rcv: ", 14400000);
        System.out.println("recsRcv=" + recsRcv.size());
        assertTrue("Sims not big enough...", recsSim.size() >= recsRcv.size());
        assertTrue("Rcv not in Sim", containsAll(recsSim, recsRcv));

        recsRcv.clear();
        readOutFile(TESTS_SCRIPTS + "rcv-test-02.out", recsRcv, "Rcv: ", 14400000);
        System.out.println("recsRcv=" + recsRcv.size());
        assertTrue("Rcv shout nothing receive", recsRcv.size() == 0);
    }

    @Test
    public void testStartWithDisconnected() {
        runScript(TESTS_SCRIPTS + "integrationTest-start-with-disconnected.sh");

        readOutFile(TESTS_SCRIPTS + "rcv-test.out", recsRcv, "Rcv: ", 14400000);
        readOutFile(TESTS_SCRIPTS + "sim-test.out", recsSim, "Sim: ", 0);

        printRecsSize();

        assertTrue("Sims not big enough...", recsSim.size() >= recsRcv.size());
        assertTrue("Rcv not in Sim", containsAll(recsSim, recsRcv));
    }

    @Test
    public void testQueue() {
        runScript(TESTS_SCRIPTS + "integrationTest-queue.sh");

        readOutFile(TESTS_SCRIPTS + "rcv-test.out", recsRcv, "Rcv: ", 14400000);
        readOutFile(TESTS_SCRIPTS + "sim-test.out", recsSim, "Sim: ", 0);

        printRecsSize();

        assertTrue("Sims not big enough...", recsSim.size() >= recsRcv.size());
        assertTrue("Rcv not in Sim", containsAll(recsSim, recsRcv));
    }

    @Test
    public void testStartLostFixes() {
        runScript(TESTS_SCRIPTS + "integrationTest-lost-fixes.sh");

        readOutFile(TESTS_SCRIPTS + "rcv-test.out", recsRcv, "Rcv: ", 14400000);
        readOutFile(TESTS_SCRIPTS + "sim-test.out", recsSim, "Sim: ", 0);

        printRecsSize();

        assertTrue("Rcv count not correct", recsRcv.size() == 80);
        assertTrue("Sims not big enough...", recsSim.size() >= recsRcv.size());
        assertTrue("Rcv not in Sim", containsAll(recsSim, recsRcv));
    }

}
