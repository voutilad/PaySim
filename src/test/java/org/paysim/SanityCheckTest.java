package org.paysim;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.paysim.parameters.Parameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * This is a naive approach to sanity checking any logic changes to the
 * PaySimState base class and its derivatives using a "gold standard"
 * output from a known good version of PaySim run for <b>8 steps</b>.
 */
public class SanityCheckTest {
    private static final String nixTestLog = "/test_rawLog.csv.gz";
    private static final String win32TestLog = "/seed_100000_win32.csv.gz";

    private Parameters parameters;
    private String testLog;

    @BeforeEach
    void setup() throws Exception {
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            testLog = win32TestLog;
        } else {
            testLog = nixTestLog;
        }
        Path path = Paths.get(getClass().getResource("/PaySim.properties").toURI());
        parameters = new Parameters(path.toString());
    }

    @Test
    void throwsExceptionIfRunningTwice() {
        Assertions.assertThrows(IllegalStateException.class, () ->{
            IteratingPaySim sim = new IteratingPaySim(parameters);
            sim.run();
            sim.run();
        });
    }

    @Test
    void canAbortASimulation() throws InterruptedException {
        final String workerName = "abortTestWorker";
        IteratingPaySim sim = new IteratingPaySim(parameters, 1, workerName);
        sim.run();
        Assertions.assertNotNull(sim.next());
        // between the above and the below lines, chances are our worker will add another to the queue
        sim.abort();

        // This should drain anything left in the queue. It's possible we ended up with another item due
        // to a TOCTOU flaw in the current version
        // XXX: this sleep()/next() is to overcome races on Win10
        Thread.sleep(200);
        sim.next();

        Assertions.assertNull(sim.next());
        Assertions.assertFalse(sim.hasNext());

        // XXX: This sucks, but we need to wait some time for the sim to finish since
        // there's no current way to pull the plug other than waiting for it to try
        Thread.sleep(500);
        int cnt = Thread.activeCount();
        Thread[] threads = new Thread[cnt];
        Thread.enumerate(threads);
        Assertions.assertFalse(Arrays.stream(threads)
                .anyMatch(thread -> thread.getName().startsWith(workerName)));
    }

    @Test
    void iteratingPaySimTracksGlobalStepOrder() throws Exception {
        IteratingPaySim sim = new IteratingPaySim(parameters);
        sim.run();
        final AtomicInteger lastStep = new AtomicInteger(0);
        sim.forEachRemaining(tx ->
            Assertions.assertTrue(
                    tx.getGlobalStep() == lastStep.incrementAndGet(),
                    "Each step is 1 greater than the last"));
    }

    @Test
    void sanityCheckIteratingPaySim() throws Exception {
        Path path = Paths.get(getClass().getResource(testLog).toURI());
        GZIPInputStream gzis = new GZIPInputStream(Files.newInputStream(path));
        BufferedReader reader = new BufferedReader(new InputStreamReader(gzis));

        IteratingPaySim sim = new IteratingPaySim(parameters);
        sim.run();

        // XXX: yes, this reads all lines into memory...
        for (String line : reader.lines().collect(Collectors.toList())) {
            Assertions.assertEquals(line, sim.next().toString());
        }

        // This order is fragile due to current hasNext() logic.
        Assertions.assertNull(sim.next());
        Assertions.assertFalse(sim.hasNext());
    }
}
