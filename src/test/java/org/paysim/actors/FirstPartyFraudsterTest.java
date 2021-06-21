package org.paysim.actors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.paysim.IteratingPaySim;
import org.paysim.identity.ClientIdentity;
import org.paysim.parameters.Parameters;

import java.util.HashSet;

public class FirstPartyFraudsterTest {
    private Parameters parameters;

    @BeforeEach
    void setup() {
        parameters = new Parameters("PaySim.properties");
    }

    @Test
    void allClientsShouldBeUnique() throws Exception {
        // XXX: setup for this test sucks, run sim with large queue, drain sim. Should use a testing paysim version.
        IteratingPaySim sim = new IteratingPaySim(parameters, 100000);
        sim.run();
        sim.forEachRemaining(t -> {
        });

        ClientIdentity fpfIdentity = sim.generateIdentity();
        FirstPartyFraudster fpf = new FirstPartyFraudster(sim, fpfIdentity, 2);

        // Run 9 times...should generate collision guaranteed if bad logic is in place
        for (int i = 0; i < 9; i++) {
            fpf.commitFraud(sim);
        }

        Assertions.assertEquals(9, fpf.fauxAccounts.size());
        final HashSet<String> fakeIds = new HashSet<>();
        fpf.fauxAccounts.forEach(acct -> fakeIds.add(acct.getId()));
        Assertions.assertEquals(9, fakeIds.size());
    }
}
