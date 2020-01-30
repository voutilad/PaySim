package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.base.Transaction;
import org.paysim.identity.ClientIdentity;
import org.paysim.identity.HasClientIdentity;
import org.paysim.identity.Identifiable;
import org.paysim.identity.Identity;
import org.paysim.output.Output;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirstPartyFraudster extends SuperActor implements HasClientIdentity, Identifiable, Steppable {
    private double profit = 0;

    private final Mule mule;
    private final ClientIdentity identity;
    private final List<ClientIdentity> identities;
    private final List<SuperActor> fauxAccounts;

    public FirstPartyFraudster(PaySimState state, ClientIdentity identity) {
        // TODO: come up with a default for num of faux identities
        this(state, identity, 3);
    }

    public FirstPartyFraudster(PaySimState state, ClientIdentity identity, int numIdentities) {
        super(state);
        this.identity = identity;
        fauxAccounts = new ArrayList<>();
        identities = new ArrayList<>();

        for (int i=0; i<numIdentities; i++) {
            identities.add(state.generateIdentity());
        }
        mule = new Mule(state, this.identity);
    }

    @Override
    public Type getType() {
        return Type.FIRST_PARTY_FRAUDSTER;
    }

    @Override
    public void step(SimState state) {
        PaySimState paysim = (PaySimState) state;

        if (paysim.getRNG().nextDouble() < parameters.fraudProbability) {

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

    @Override
    public String getId() {
        return identity.id;
    }

    @Override
    public String getName() {
        return identity.name;
    }

    @Override
    public Identity getIdentity() {
        return identity;
    }

    @Override
    public ClientIdentity getClientIdentity() {
        return identity;
    }

    @Override
    public Map<String, String> getIdentityAsMap() {
        return identity.asMap();
    }
}
