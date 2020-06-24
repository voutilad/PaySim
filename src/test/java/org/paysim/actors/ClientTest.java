package org.paysim.actors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.paysim.IteratingPaySim;
import org.paysim.parameters.Parameters;

public class ClientTest {

    Parameters parameters;

    @BeforeEach
    void setup() {
        parameters = new Parameters("PaySim.properties");
    }

    @Test
    void testPickingRandomMerchants() {
        IteratingPaySim sim = new IteratingPaySim(parameters, 100);

    }
}
