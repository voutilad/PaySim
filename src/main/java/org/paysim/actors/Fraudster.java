package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.base.Transaction;
import org.paysim.output.Output;
import org.paysim.parameters.Parameters;
import sim.engine.SimState;
import sim.engine.Steppable;

import java.util.ArrayList;

public class Fraudster extends SuperActor implements Steppable {
    private static final String FRAUDSTER_IDENTIFIER = "C";
    private double profit = 0;
    private int nbVictims = 0;

    public Fraudster(String name, Parameters parameters) {
        super(FRAUDSTER_IDENTIFIER + name, parameters);
    }

    @Override
    public Type getType() {
        return Type.FRAUDSTER;
    }

    @Override
    public void step(SimState state) {
        ArrayList<Transaction> transactions = new ArrayList<>();
        PaySimState paysim = (PaySimState) state;
        int step = (int) state.schedule.getSteps();

        if (paysim.random.nextDouble() < parameters.fraudProbability) {
            Client c = paysim.pickRandomClient(getName());
            c.setFraud(true);
            double balance = c.getBalance();
            // create mule client
            if (balance > 0) {
                int nbTransactions = (int) Math.ceil(balance / parameters.transferLimit);
                for (int i = 0; i < nbTransactions; i++) {
                    boolean transferFailed;
                    Mule muleClient = new Mule(paysim.generateId(), paysim.pickRandomBank(), parameters);
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
                    nbVictims++;
                    paysim.addClient(muleClient);
                    if (transferFailed)
                        break;
                }
            }
            c.setFraud(false);
        }
    }

    @Override
    public String toString() {
        ArrayList<String> properties = new ArrayList<>();

        properties.add(getName());
        properties.add(Integer.toString(nbVictims));
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, profit));

        return String.join(Output.OUTPUT_SEPARATOR, properties);
    }
}
