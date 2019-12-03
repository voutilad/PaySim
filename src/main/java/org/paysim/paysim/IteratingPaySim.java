package org.paysim.paysim;

import org.paysim.paysim.base.Transaction;
import org.paysim.paysim.parameters.Parameters;

import java.util.*;
import java.util.function.Consumer;

public class IteratingPaySim extends PaySimState implements Iterator<Transaction> {

    public IteratingPaySim() {
        super(Parameters.getSeed());

        // XXX: for now, force it to only do 4 while I work this out.
        Parameters.nbSteps = 4;
    }

    public static void main(String[] args) {
        System.out.println("Starting an instance of IteratingPaySim...");
        Parameters.initParameters("PaySim.properties");

        IteratingPaySim sim = new IteratingPaySim();
        sim.runSimulation();
        System.out.println("Bye 👋");
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Transaction next() {
        return null;
    }

    @Override
    public void remove() {

    }

    @Override
    public void forEachRemaining(Consumer<? super Transaction> action) {

    }

    @Override
    public void onTransactions(List<Transaction> transactions) {
        transactions.stream().forEachOrdered(tx -> {
            System.out.println(tx.toString());
        });
    }

    @Override
    public boolean onStep(long stepNum) {
        return true;
    }

}
