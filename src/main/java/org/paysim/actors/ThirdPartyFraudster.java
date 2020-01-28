package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.base.Transaction;
import org.paysim.identity.Identity;
import org.paysim.output.Output;
import org.paysim.parameters.Parameters;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThirdPartyFraudster extends SuperActor implements Steppable {
    private static final String FRAUDSTER_IDENTIFIER = "F3";
    private double profit = 0;
    private final List<SuperActor> victims;
    private final List<Merchant> favoredMerchants;

    public ThirdPartyFraudster(PaySimState state, Identity identity) {
        this(state.generateId(), state, identity);
    }

    public ThirdPartyFraudster(String id, PaySimState state, Identity identity) {
        super(FRAUDSTER_IDENTIFIER + "-" + id, state);

        victims = new ArrayList<>();
        favoredMerchants = new ArrayList<>();

        // For now, we materialize the Identity into the property map
        this.setProperty(Properties.PHONE, identity.phoneNumber);
        this.setProperty(Properties.NAME, identity.name);
        this.setProperty(Properties.EMAIL, identity.email);
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

            // XXX: historical logging for helping us understand PaySim a bit more
            c.setProperty(Properties.HISTORY,
                    c.getPropertyOrDefault(Properties.HISTORY, "") + this.getId() + ";");

            double balance = c.getBalance();
            // create mule client
            if (balance > 0) {
                int nbTransactions = (int) Math.ceil(balance / parameters.transferLimit);
                for (int i = 0; i < nbTransactions; i++) {
                    boolean transferFailed;
                    Identity muleIdentity = paysim.generateIdentity();
                    Mule muleClient = new Mule(paysim, muleIdentity);
                    muleClient.setFraud(true);
                    if (balance > parameters.transferLimit) {
                        Transaction t = c.handleTransfer(paysim, step, parameters.transferLimit, muleClient);
                        transferFailed = !t.isSuccessful();
                        balance -= parameters.transferLimit;
                        transactions.add(t);
                    } else {
                        Transaction t = c.handleTransfer(paysim, step, balance, muleClient);
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
}
