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

/**
 * Hi, I'm Theo...the 3rd Party Fraudster. I like to acquire your account creds and steal your cash/credit.
 */
public class ThirdPartyFraudster extends SuperActor implements HasClientIdentity, Identifiable, Steppable {
    private double profit = 0;
    private final ClientIdentity identity;
    private final Mule mule;
    private final Set<Client> victims;
    private final Set<Merchant> favoredMerchants;

    public ThirdPartyFraudster(PaySimState state, ClientIdentity identity) {
        super(state);
        this.identity = identity;
        victims = new HashSet<>();
        favoredMerchants = new HashSet<>();

        mule = new Mule(state, identity);
        state.addClient(mule);
    }

    /**
     * These are the merchants I've found a way to breach...or maybe in cahoots with.
     *
     * @param m a Merchant in the simulation
     * @return {@code true} (as specified by {@link Collection#add})
     */
    public boolean addFavoredMerchant(Merchant m) {
        return favoredMerchants.add(m);
    }

    protected double pickTestChargeAmount(PaySimState state, Client victim, String actionType) {
        final double wobble = 1 + (1f / (state.getRNG().nextInt(50) + 1));
        final double avgAmountForAction = victim.getClientProfile().getProfilePerAction(actionType).getAvgAmount();
        return avgAmountForAction * 0.25 * wobble;
    }

    protected Merchant pickTestMerchant(PaySimState state) {
        final int merchantPopulation = state.getMerchants().size();
        Merchant m = state.getMerchants().get(state.getRNG().nextInt(merchantPopulation));
        while (favoredMerchants.contains(m)) {
            m = state.getMerchants().get(state.getRNG().nextInt(merchantPopulation));
        }
        return m;
    }

    protected Optional<Merchant> pickFavoredMerchant(PaySimState state) {
        final int numMerchants = favoredMerchants.size();
        if (numMerchants > 0) {
            final int choice = state.getRNG().nextInt(numMerchants);
            return Optional.of(favoredMerchants.toArray(new Merchant[numMerchants])[choice]);
        }
        return Optional.empty();
    }

    protected Client pickTargetClient(PaySimState state) {
        Optional<Merchant> maybeMerchant = pickFavoredMerchant(state);
        Merchant m = maybeMerchant.orElse(state.pickRandomMerchant());

        if (m.getRecentClients().size() > 1) {
            Client c = m.getRecentClients().get(state.getRNG().nextInt(m.getRecentClients().size()));
            if (c.getId() != this.getId()) {
                // XXX: In practice this may not happen since we currently don't let Fraudsters perform
                // transactions with anyone directly, but just to be safe.
                return c;
            }
        }
        return state.pickRandomClient(getId());
    }

    protected Optional<Client> pickRepeatVictim(PaySimState state) {
        final int numVictims = victims.size();
        if (numVictims > 0) {
            final int choice = state.getRNG().nextInt(numVictims);
            return Optional.of(victims.toArray(new Client[numVictims])[choice]);
        }
        return Optional.empty();
    }

    @Override
    public Type getType() {
        return Type.THIRD_PARTY_FRAUDSTER;
    }

    @Override
    public void step(SimState state) {
        PaySimState paysim = (PaySimState) state;
        ArrayList<Transaction> transactions = new ArrayList<>();
        int step = (int) state.schedule.getSteps();

        // XXX: Core 3rd Party Fraud Logic
        if (paysim.getRNG().nextDouble() < parameters.thirdPartyFraudProbability) {
            if (victims.isEmpty() || paysim.getRNG().nextBoolean(parameters.thirdPartyNewVictimProbability)) {
                // Time to find a new lucky victim
                Client c = pickTargetClient(paysim);
                Merchant m = pickTestMerchant(paysim);
                final double testChargeAmt = pickTestChargeAmount(paysim, c, Client.PAYMENT);
                Transaction testCharge = c.handlePayment(m, step, testChargeAmt);
                testCharge.setFraud(true);

                if (testCharge.isSuccessful()) {
                    victims.add(c);
                    transactions.add(testCharge);
                    Transaction xfer = c.handleTransfer(mule, step, pickTestChargeAmount(paysim, c, Client.TRANSFER));
                    xfer.setFraud(true);
                    if (xfer.isSuccessful()) {
                        transactions.add(xfer);
                    }
                }
            } else {
                // Repeat attack on a victim
                pickRepeatVictim(paysim).ifPresent(c -> {
                    Transaction xfer = c.handleTransfer(mule, step, pickTestChargeAmount(paysim, c, Client.TRANSFER));
                    xfer.setFraud(true);
                    if (xfer.isSuccessful()) {
                        transactions.add(xfer);
                    }
                });
            }
        }

        // Right now, we need to always check our Mule accounts to see if we want to cash them out. Mules
        // are brainless because they're unscheduled actors
        if (paysim.getRNG().nextBoolean(0.3)) {
            mule.fraudulentCashOut(paysim, step);
        }
        paysim.onTransactions(transactions);
    }

    @Override
    public String toString() {
        ArrayList<String> properties = new ArrayList<>();
        final Set<String> uniqueVictims = new HashSet<>();
        victims.forEach(v -> uniqueVictims.add(v.getId()));

        properties.add(getId());
        properties.add(getType().toString());
        properties.add(Integer.toString(uniqueVictims.size()));
        properties.add(String.format("[%s]", String.join(",", uniqueVictims)));
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
    public Map<String, Object> getIdentityAsMap() {
        return identity.asMap();
    }
}
