package org.paysim.paysim.actors.networkdrugs;

import ec.util.MersenneTwisterFast;
import org.paysim.paysim.PaySimState;
import org.paysim.paysim.base.Transaction;
import org.paysim.paysim.parameters.Parameters;

import org.paysim.paysim.actors.Client;
import org.paysim.paysim.utils.RandomCollection;
import sim.engine.SimState;

public class DrugConsumer extends Client {
    private DrugDealer dealer;
    private RandomCollection<Double> probAmountProfile;
    private double probabilityBuy;

    public DrugConsumer(PaySimState paySim, DrugDealer dealer, double monthlySpending, RandomCollection<Double> probAmountProfile, double meanTr) {
        super(paySim);
        this.dealer = dealer;
        this.probAmountProfile = probAmountProfile;
        this.probabilityBuy = monthlySpending / meanTr / Parameters.nbSteps;
    }

    @Override
    public void step(SimState state) {
        PaySimState paySim = (PaySimState) state;
        int step = (int) paySim.schedule.getSteps();

        super.step(state);

        if (wantsToBuyDrugs(paySim.random)) {
            double amount = pickAmount();

            handleTransferDealer(paySim, step, amount);
        }
    }

    private Transaction handleTransferDealer(PaySimState paySim, int step, double amount) {
        Transaction t = handleTransfer(paySim, step, amount, dealer);

        if (t.isSuccessful()) {
            dealer.addMoneyFromDrug(amount);
        }

        return t;
    }

    private boolean wantsToBuyDrugs(MersenneTwisterFast random) {
        return random.nextBoolean(probabilityBuy);
    }

    private double pickAmount() {
        return probAmountProfile.next();
    }
}
