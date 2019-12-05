package org.paysim;

import org.paysim.base.Transaction;
import org.paysim.parameters.Parameters;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

public class IteratingPaySim extends PaySimState implements Iterator<Transaction> {

    private BlockingQueue<Transaction> queue;
    private static int QUEUE_DEPTH = 100;
    private boolean running = false;

    public IteratingPaySim() {
        super(Parameters.getSeed());
        this.queue = new ArrayBlockingQueue<>(QUEUE_DEPTH);

        // XXX: for now, force it to only do 4 while I work this out.
        Parameters.nbSteps = 4;
    }

    private class Worker implements Runnable {
        public boolean timeToDie = false;

        @Override
        public void run() {
            try {
                while (!timeToDie) {
                    Transaction t = queue.poll(5, TimeUnit.SECONDS);
                    System.out.println(t.toString());
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted during simulation");
            }
        }
    }

    @Override
    public void run() {
        Worker w = new Worker();
        Thread t = new Thread(w);
        try {
            t.start();
            this.runSimulation();
            w.timeToDie = true;
            t.join(5000);
        } catch (InterruptedException e) {
            System.err.println(String.format("%s interrupted", this.getClass().getName()));
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting an instance of IteratingPaySim...");
        Parameters.initParameters("PaySim.properties");

        IteratingPaySim sim = new IteratingPaySim();
        sim.run();
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
        transactions.forEach(tx -> {
            try {
                this.queue.put(tx);
            } catch (InterruptedException e) {
                System.err.println("Interrupted while adding tx to queue, skipping.");
            }
        });
    }

    @Override
    public boolean onStep(long stepNum) {
        return true;
    }

}
