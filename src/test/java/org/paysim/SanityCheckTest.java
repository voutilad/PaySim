package org.paysim;

import com.devskiller.jfairy.Bootstrap;
import com.devskiller.jfairy.Fairy;
import com.devskiller.jfairy.producer.person.Person;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.paysim.base.Transaction;
import org.paysim.parameters.Parameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * This is a naive approach to sanity checking any logic changes to the
 * PaySimState base class and its derivatives using a "gold standard"
 * output from a known good version of PaySim run for <b>10 steps</b>.
 */
public class SanityCheckTest {
    private static final String testLog = "/test_rawLog.csv.gz";

    private Parameters parameters;

    @BeforeEach
    void setup() {
        parameters = new Parameters("PaySim.properties");
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
        IteratingPaySim sim = new IteratingPaySim(parameters, 1);
        sim.run();
        Assertions.assertNotNull(sim.next());
        // between the above and the below lines, chances are our worker will add another to the queue
        sim.abort();

        // This should drain anything left in the queue. It's possible we ended up with another item due
        // to a TOCTOU flaw in the current version
        // XXX: this sleep()/next() is to overcome races on Win10
        Thread.sleep(100);
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
                .anyMatch(thread -> thread.getName().startsWith(IteratingPaySim.WORKER_NAME)));
    }

    @Test
    void iteratingPaySimTracksGlobalStepOrder() throws Exception {
        IteratingPaySim sim = new IteratingPaySim(parameters, 1);
        sim.run();
        final AtomicInteger lastStep = new AtomicInteger(0);
        sim.forEachRemaining(tx ->
            Assertions.assertTrue(
                    tx.getGlobalStep() == lastStep.incrementAndGet(),
                    "Each step is 1 greater than the last"));
    }

    @Test
    @Disabled
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

    /**
     * While implementing routines to generate unique client ids, it seems mules are ending
     * up with conflicting identifiers at the moment. Brute force test for it.
     */
    @Test
    void sanityCheckClientIdGeneration() {
        PaySimState state = new PaySimState(parameters) {
            @Override
            public boolean onTransactions(List<Transaction> transactions) {
                return false;
            }

            @Override
            protected boolean onStep(long stepNum) {
                return false;
            }

            @Override
            public void run() {

            }
        };

        for (int i=0; i<20000; i++) {
            state.generateUniqueClientId();
        }
        Assertions.assertEquals(20000, state.getClientIds().size());

    }
}
