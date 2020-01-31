package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.base.Transaction;
import org.paysim.identity.*;
import org.paysim.output.Output;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Hi, I'm Finley...the First Party Fraudster!
 */
public class FirstPartyFraudster extends SuperActor implements HasClientIdentity, Identifiable, Steppable {
    private double profit = 0;

    private final Mule cashoutMule;
    private final ClientIdentity identity;
    private final List<ClientIdentity> identities;
    private final List<Mule> fauxAccounts;

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
        cashoutMule = new Mule(state, this.identity);
        state.addClient(cashoutMule);
    }

    @Override
    public void step(SimState state) {
        PaySimState paysim = (PaySimState) state;
        final int step = (int) state.schedule.getSteps();

        if (paysim.getRNG().nextDouble() < parameters.fraudProbability) {
            ClientIdentity fauxIdentity = composeNewIdentity(paysim);
            Mule m = new Mule(paysim, fauxIdentity);

            Transaction drain = m.handleTransfer(cashoutMule, step, m.balance);
            fauxAccounts.add(m);
            paysim.addClient(m);
            paysim.onTransactions(Arrays.asList(drain));
        }
    }

    /**
     * Create a fake identity, composing from those created initially by the Fraudster.
     *
     * TODO: this is a candidate for a refactor for a few reasons
     * @param state the current PaySimState
     * @return new ClientIdentity or the only ClientIdentity originally created, null otherwise :-(
     */
    protected ClientIdentity composeNewIdentity(PaySimState state) {
        final int identityCnt = identities.size();
        if (identityCnt > 1) {
            final int choice = state.getRNG().nextInt(identityCnt);
            ClientIdentity baseIdentity = identities.get(choice);

            // TODO: this is a gross mess, but let's risk infinite loops for now to pick another identity
            int otherChoice = state.getRNG().nextInt(identityCnt);
            while (otherChoice == choice) {
                otherChoice = state.getRNG().nextInt(identityCnt);
            }
            ClientIdentity otherIdentity = identities.get(otherChoice);

            /* XXX: now we pick a property to re-purpose from one to the other...
                    let's assume 0 -> ssn, 1 -> email, 2 -> phone
             */
            switch (state.getRNG().nextInt(2)) {
                case 0: return baseIdentity.replaceProperty(Properties.SSN, otherIdentity.ssn);
                case 1: return baseIdentity.replaceProperty(Properties.EMAIL, otherIdentity.email);
                case 2: return baseIdentity.replaceProperty(Properties.PHONE, otherIdentity.phoneNumber);
            }
        } else if (identityCnt == 1) {
            return identities.get(0);
        }

        return null;
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
    public Type getType() {
        return Type.FIRST_PARTY_FRAUDSTER;
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
