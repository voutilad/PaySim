package org.paysim;

import org.paysim.base.Transaction;
import org.paysim.parameters.Parameters;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class IteratingPaySim extends PaySimState implements Iterator<Transaction> {

    private BlockingQueue<Transaction> queue;
    private static int QUEUE_DEPTH = 200_000;
    private SimulationWorker worker;

    public IteratingPaySim() {
        super(Parameters.getSeed());
        this.queue = new ArrayBlockingQueue<>(QUEUE_DEPTH);
        this.worker = new SimulationWorker(this);
    }

    private class SimulationWorker extends Thread {
        private PaySimState state;
        private volatile boolean running = false;

        public SimulationWorker(PaySimState state) {
            this.state = state;
        }

        @Override
        public void run() {
            running = true;
            state.runSimulation();
            running = false;
        }

        public boolean isRunning() {
            return this.running;
        }
    }

    @Override
    public void run() {
        if (!worker.isRunning()) {
            worker.start();
        } else {
            String msg = String.format("SimulationWorker %s is already started", worker);
            throw new IllegalStateException(msg);
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting an instance of IteratingPaySim...");
        Parameters.initParameters("PaySim.properties");

        long startTime = System.currentTimeMillis();
        IteratingPaySim sim = new IteratingPaySim();
        sim.run();
        //sim.forEachRemaining(tx -> System.out.println(tx.toString()));
        sim.forEachRemaining(tx -> { });
        long totalTime = System.currentTimeMillis() - startTime;

        System.out.println("Duration: " + totalTime / 1000.0 + " seconds");
        System.out.println("Bye 👋");
    }

    @Override
    public boolean hasNext() {
        // XXX: assume thread running -> more data coming...this is a broken design
        return worker.isRunning();
    }

    @Override
    public Transaction next() {
        Transaction tx = null;

        while (worker.isRunning() && tx == null) {
            try {
                tx = queue.poll(500, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new NoSuchElementException();
            }
        }
        return tx;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(Consumer<? super Transaction> action) {
        Objects.requireNonNull(action);
        while (hasNext()) {
            try {
                Transaction tx = next();
                if (tx != null) {
                    action.accept(tx);
                }
            } catch (NoSuchElementException nse) {
                // XXX: End of simulation??
            }
        }

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
