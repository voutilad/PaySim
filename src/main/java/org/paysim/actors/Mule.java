package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.base.Transaction;
import org.paysim.identity.ClientIdentity;

public class Mule extends Client {

    public Mule(PaySimState state, ClientIdentity identity) {
        super(state, identity);
        overdraftLimit = 0;
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
