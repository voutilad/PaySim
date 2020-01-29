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

import java.util.*;

public class ThirdPartyFraudster extends SuperActor implements HasClientIdentity, Identifiable, Steppable {
    private double profit = 0;
    private final ClientIdentity identity;
    private final List<SuperActor> victims;
    private final List<Merchant> favoredMerchants;

    public ThirdPartyFraudster(PaySimState state, ClientIdentity identity) {
        super(state);
        this.identity = identity;
        victims = new ArrayList<>();
        favoredMerchants = new ArrayList<>();
    }

    public boolean addFavoredMerchant(Merchant m) {
        return favoredMerchants.add(m);
    }

    protected Client pickTargetClient(PaySimState state) {
        // First we try to identify a client via a Merchant we know about, 95% of the time
        if (favoredMerchants.size() > 0 && state.getRNG().nextBoolean(0.95)) {
            Merchant m = favoredMerchants.get(state.getRNG().nextInt(favoredMerchants.size()));
            if (m.getRecentClients().size() > 1) {
                Client c = m.getRecentClients().get(state.getRNG().nextInt(m.getRecentClients().size()));
                if (c.getId() != this.getId()) {
                    // XXX: In practice this may not happen since we currently don't let Fraudsters perform
                    // transactions with anyone directly, but just to be safe.
                    return c;
                }
            }
        }

        // Otherwise, we pick at random.
        return state.pickRandomClient(getId());
    }

    @Override
    public Type getType() {
        return Type.THIRD_PARTY_FRAUDSTER;
    }

    @Override
    public void step(SimState state) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        PaySimState paysim = (PaySimState) state;
        int step = (int) state.schedule.getSteps();

        if (paysim.getRNG().nextDouble() < parameters.fraudProbability) {
            Client c = pickTargetClient(paysim);
            c.setFraud(true);

            double balance = c.getBalance();
            // create mule client
            if (balance > 0) {
                int nbTransactions = (int) Math.ceil(balance / parameters.transferLimit);
                for (int i = 0; i < nbTransactions; i++) {
                    boolean transferFailed;
                    ClientIdentity muleIdentity = paysim.generateIdentity();
                    Mule muleClient = new Mule(paysim, muleIdentity);
                    muleClient.setFraud(true);
                    if (balance > parameters.transferLimit) {
                        Transaction t = c.handleTransfer(muleClient, step, parameters.transferLimit);
                        transferFailed = !t.isSuccessful();
                        balance -= parameters.transferLimit;
                        transactions.add(t);
                    } else {
                        Transaction t = c.handleTransfer(muleClient, step, balance);
                        transferFailed = !t.isSuccessful();
                        balance = 0;
                        transactions.add(t);
                    }

                    profit += muleClient.getBalance();
                    transactions.add(muleClient.fraudulentCashOut(paysim, step, muleClient.getBalance()));
                    paysim.addClient(muleClient);
                    if (transferFailed)
                        break;
                }
            }
            c.setFraud(false);
            victims.add(c);
            paysim.onTransactions(transactions);
        }
    }

    @Override
    public String toString() {
        ArrayList<String> properties = new ArrayList<>();
        final Set<String> uniqueVictims = new HashSet<>();
        victims.forEach(v -> uniqueVictims.add(v.getId()));

        properties.add(getId());
        properties.add(getType().toString());
        properties.add(Integer.toString(uniqueVictims.size()));
        properties.add(String.format("[%s]", String.join(",", uniqueVictims.toArray(new String[uniqueVictims.size()]))));
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, profit));

        return String.join(Output.OUTPUT_SEPARATOR, properties);
    }

    @Override
    public ClientIdentity getClientIdentity() {
        return identity;
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
    public Map<String, String> getIdentityAsMap() {
        return identity.asMap();
    }
}
