package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.base.Transaction;
import org.paysim.parameters.Parameters;

public class Mule extends Client {
    private static final String MULE_IDENTIFIER = "C";

    public Mule(String name, Bank bank, Parameters parameters) {
        super(MULE_IDENTIFIER + name, bank, parameters);
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
