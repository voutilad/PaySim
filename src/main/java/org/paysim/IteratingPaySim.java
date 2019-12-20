package org.paysim;

import org.paysim.base.Transaction;
import org.paysim.parameters.Parameters;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class IteratingPaySim extends PaySimState implements Iterator<Transaction> {

    private BlockingQueue<Transaction> queue;
    private static int QUEUE_DEPTH = 200_000;

    private SimulationWorker worker;
    protected static String WORKER_NAME = "SimulationWorker";

    private AtomicBoolean running = new AtomicBoolean();

    public IteratingPaySim(Parameters parameters, int queueDepth) {
        super(parameters);
        this.queue = new ArrayBlockingQueue<>(queueDepth);
        worker = new SimulationWorker(this);
    }

    public IteratingPaySim(Parameters parameters) {
        this(parameters, QUEUE_DEPTH);
    }

    private class SimulationWorker implements Runnable {
        private IteratingPaySim state;
        public SimulationWorker(IteratingPaySim state) {
            this.state = state;
        }

        @Override
        public void run() {
            state.runSimulation();
            state.setRunning(false);
        }
    }

    @Override
    public synchronized void run() {
        if (running.compareAndSet(false, true)) {
            final Thread t = new Thread(worker, WORKER_NAME);
            t.start();
        } else {
            String msg = String.format("SimulationWorker %s is already started", worker);
            throw new IllegalStateException(msg);
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting an instance of IteratingPaySim...");
        Parameters parameters = new Parameters("PaySim.properties");

        long startTime = System.currentTimeMillis();
        IteratingPaySim sim = new IteratingPaySim(parameters);
        sim.run();
        sim.forEachRemaining(tx -> System.out.println(tx.toString()));
        long totalTime = System.currentTimeMillis() - startTime;

        System.out.println("Duration: " + totalTime / 1000.0 + " seconds");
        System.out.println("Bye 👋");
    }

    @Override
    public boolean hasNext() {
        return !queue.isEmpty() || running.get();
    }

    @Override
    public Transaction next() {
        Transaction tx = null;

        while (hasNext() && tx == null) {
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
    public boolean onTransactions(List<Transaction> transactions) {
        if (running.get()) {
            transactions.forEach(tx -> {
                try {
                    this.queue.put(tx);
                } catch (InterruptedException e) {
                    System.err.println("Interrupted while adding tx to queue, skipping.");
                }
            });
            return true;
        }
        return false;
    }

    protected void setRunning(boolean newValue) {
        running.set(newValue);
    }

    public void abort() throws IllegalStateException {
        if (!running.compareAndSet(true, false)) {
            throw new IllegalStateException("Cannot stop simulation when state isn't running");
        }

        // XXX: a sloppy drain implementation...bad idea?
        queue.clear();
    }

    @Override
    public boolean onStep(long stepNum) {
        return running.get();
    }
}
