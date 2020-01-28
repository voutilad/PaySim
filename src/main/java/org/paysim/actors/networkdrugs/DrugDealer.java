package org.paysim.actors.networkdrugs;

import org.paysim.PaySimState;
import org.paysim.actors.Client;
import sim.engine.SimState;

public class DrugDealer extends Client {
    private double thresholdForCashOut;
    private double drugMoneyInAccount;

    public DrugDealer(PaySimState paySim, double thresholdForCashOut) {
        super(paySim.generateIdentity(), paySim);
        this.thresholdForCashOut = thresholdForCashOut;
        this.drugMoneyInAccount = 0;
    }

    @Override
    public void step(SimState state) {
        PaySimState paySim = (PaySimState) state;
        int step = (int) paySim.schedule.getSteps();

        super.step(state);

        if (wantsToCashOutProfit()) {
            double amount = pickAmountCashOutProfit();
            super.handleCashOut(paySim, step, amount);
            drugMoneyInAccount -= amount;
        }
    }

    private boolean wantsToCashOutProfit(){
        //TODO: implement a randomized version
        return drugMoneyInAccount > thresholdForCashOut;
    }

    private double pickAmountCashOutProfit(){
        //TODO: implement a randomized version
        return thresholdForCashOut;
    }

    protected void addMoneyFromDrug(double amount){
        drugMoneyInAccount += amount;
    }
}
