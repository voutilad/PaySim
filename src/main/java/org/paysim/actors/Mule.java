package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.base.Transaction;
import org.paysim.identity.ClientIdentity;
import sim.engine.SimState;

public class Mule extends Client {

    @Override
    public void step(SimState state) {
        // XXX: NOP...we override the step() method so Mule actors become brainless Clients controlled by fraudsters
    }

    public Mule(PaySimState state, ClientIdentity identity) {
        super(state, identity);
        setFraud(true);
        overdraftLimit = 0;
    }

    Transaction fraudulentCashOut(PaySimState state, int step) {
        double amount = getBalance() > state.getParameters().transferLimit ?
                state.getParameters().transferLimit : getBalance();
        return fraudulentCashOut(state, step, amount);
    }

    Transaction fraudulentCashOut(PaySimState state, int step, double amount) {
        Merchant merchantTo = state.pickRandomMerchant();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantTo.getBalance();

        this.withdraw(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantTo.getBalance();

        Transaction t = new Transaction(step, CASH_OUT, amount, this, oldBalanceOrig,
                newBalanceOrig, merchantTo, oldBalanceDest, newBalanceDest);
        t.setFraud(this.isFraud());
        return t;
    }


    @Override
    public Type getType() {
        return Type.MULE;
    }
}
