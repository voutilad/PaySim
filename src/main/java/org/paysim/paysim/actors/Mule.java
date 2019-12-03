package org.paysim.paysim.actors;

import org.paysim.paysim.PaySimState;
import org.paysim.paysim.base.Transaction;

public class Mule extends Client {
    private static final String MULE_IDENTIFIER = "C";

    public Mule(String name, Bank bank) {
        super(MULE_IDENTIFIER + name, bank);
        this.overdraftLimit = 0;
    }

    Transaction fraudulentCashOut(PaySimState state, int step, double amount) {
        String action = "CASH_OUT";

        Merchant merchantTo = state.pickRandomMerchant();
        double oldBalanceOrig = this.getBalance();
        double oldBalanceDest = merchantTo.getBalance();

        this.withdraw(amount);

        double newBalanceOrig = this.getBalance();
        double newBalanceDest = merchantTo.getBalance();

        Transaction t = new Transaction(step, action, amount, this, oldBalanceOrig,
                newBalanceOrig, merchantTo, oldBalanceDest, newBalanceDest);
        t.setFraud(this.isFraud());
        return t;
    }

    @Override
    public Type getType() {
        return Type.MULE;
    }
}
