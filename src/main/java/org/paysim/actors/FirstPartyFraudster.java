package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.base.Transaction;
import org.paysim.identity.*;
import org.paysim.identity.Properties;
import org.paysim.output.Output;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hi, I'm Finley...the First Party Fraudster!
 */
public class FirstPartyFraudster extends SuperActor implements HasClientIdentity, Identifiable, Steppable {
    private double profit = 0;

    protected final Mule cashoutMule;
    private final ClientIdentity identity;
    protected final List<ClientIdentity> identities;
    protected final List<Mule> fauxAccounts;

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

    protected void commitFraud(PaySimState paysim) {
        Optional<ClientIdentity> maybeFauxIdentity = composeNewIdentity(paysim);

        if (maybeFauxIdentity.isPresent()) {
            Mule m = new Mule(paysim, maybeFauxIdentity.get());
            final int step = (int) paysim.schedule.getSteps();

            Transaction drain = m.handleTransfer(cashoutMule, step, m.balance);
            fauxAccounts.add(m);
            paysim.addClient(m);
            paysim.onTransactions(Arrays.asList(drain));
        }
    }

    @Override
    public void step(SimState state) {
        PaySimState paysim = (PaySimState) state;

        if (paysim.getRNG().nextDouble() < parameters.fraudProbability) {
            commitFraud(paysim);
        }
    }

    /**
     * Create a fake identity, composing from those created initially by the Fraudster.
     *
     * TODO: this is a candidate for a refactor for a few reasons
     * @param state the current PaySimState
     * @return new ClientIdentity or the only ClientIdentity originally created, null otherwise :-(
     */
    protected Optional<ClientIdentity> composeNewIdentity(PaySimState state) {
        final int identityCnt = identities.size();
        if (identityCnt > 0) {
            // Generate a new "unique" base identity that we'll mutate with stolen/synthetic identifiers
            ClientIdentity baseIdentity = state.generateIdentity();

            final int ssnChoice = state.getRNG().nextInt(identityCnt);
            final int emailChoice = state.getRNG().nextInt(identityCnt);
            final int phoneChoice = state.getRNG().nextInt(identityCnt);

            return Optional.of(baseIdentity
                    .replaceProperty(Properties.SSN, identities.get(ssnChoice).ssn)
                    .replaceProperty(Properties.EMAIL, identities.get(emailChoice).email)
                    .replaceProperty(Properties.PHONE, identities.get(phoneChoice).phoneNumber));

        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        ArrayList<String> properties = new ArrayList<>();

        properties.add(getId());
        properties.add(getType().toString());
        properties.add(Integer.toString(fauxAccounts.size()));
        properties.add(
                String.format("[%s]", String.join(",",
                        fauxAccounts.stream()
                                .map(Mule::toString).collect(Collectors.toList()))));

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
