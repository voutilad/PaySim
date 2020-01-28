package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.base.Transaction;
import org.paysim.identity.Identity;
import org.paysim.output.Output;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.List;

public class FirstPartyFraudster extends SuperActor implements Steppable {
    private static final String FRAUDSTER_IDENTIFIER = "F1";
    private double profit = 0;

    private final List<Identity> identities;
    private final List<SuperActor> fauxAccounts;

    public FirstPartyFraudster(PaySimState state, Identity identity) {
        this(state.generateId(), state, identity);
    }

    public FirstPartyFraudster(String id, PaySimState state, Identity identity) {
        // TODO: come up with a default for num of faux identities
        this(id, state, identity, 3);
    }

    public FirstPartyFraudster(String id, PaySimState state, Identity identity, int numIdentities) {
        super(id, state);
        fauxAccounts = new ArrayList<>();
        identities = new ArrayList<>();

        for (int i=0; i<numIdentities; i++) {
            identities.add(state.generateIdentity());
        }

        // For now, we materialize the Identity into the property map
        this.setProperty(Properties.PHONE, identity.phoneNumber);
        this.setProperty(Properties.NAME, identity.name);
        this.setProperty(Properties.EMAIL, identity.email);
    }

    @Override
    public Type getType() {
        return Type.FIRST_PARTY_FRAUDSTER;
    }

    @Override
    public void step(SimState state) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        PaySimState paysim = (PaySimState) state;

        if (paysim.getRNG().nextDouble() < parameters.fraudProbability) {
            // TODO: implement fraud logic...lol
        }
    }

    @Override
    public String toString() {
        ArrayList<String> properties = new ArrayList<>();

        properties.add(getId());
        properties.add(getType().toString());
        properties.add(Integer.toString(fauxAccounts.size()));
        properties.add(String.format("[%s]", String.join(",", fauxAccounts.toArray(new String[fauxAccounts.size()]))));
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, profit));

        return String.join(Output.OUTPUT_SEPARATOR, properties);
    }
}
