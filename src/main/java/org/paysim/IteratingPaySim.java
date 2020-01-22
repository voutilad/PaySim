package org.paysim;

import org.paysim.base.Transaction;
import org.paysim.parameters.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class IteratingPaySim extends PaySimState implements Iterator<Transaction> {

    private final Logger logger = LoggerFactory.getLogger(IteratingPaySim.class);
    private BlockingQueue<Transaction> queue;
    private static int QUEUE_DEPTH = 200_000;

    private SimulationWorker worker;
    protected static String WORKER_NAME = "SimulationWorker";

    private AtomicBoolean running = new AtomicBoolean();
    private AtomicInteger stepCounter = new AtomicInteger(0);

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
        private final Logger logger = LoggerFactory.getLogger(SimulationWorker.class);

        public SimulationWorker(IteratingPaySim state) {
            this.state = state;
        }

        @Override
        public void run() {
            logger.debug("starting");
            state.runSimulation();
            state.setRunning(false);
            logger.debug("finished");
        }
    }

    @Override
    public synchronized void run() {
        if (running.compareAndSet(false, true)) {
            stepCounter.set(0);
            final Thread t = new Thread(worker, WORKER_NAME);
            t.start();
            logger.debug(String.format("started worker thread: %s", t.getName()));
        } else {
            final String msg = String.format("SimulationWorker %s is already started", worker);
            logger.warn(msg);
            throw new IllegalStateException(msg);
        }
    }

    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(IteratingPaySim.class);
        logger.info("Starting an instance of IteratingPaySim...");
        Parameters parameters = new Parameters("PaySim.properties");

        long startTime = System.currentTimeMillis();
        IteratingPaySim sim = new IteratingPaySim(parameters);
        sim.run();
        sim.forEachRemaining(tx -> logger.info(tx.getGlobalStep() + "," + tx.toString()));
        long totalTime = System.currentTimeMillis() - startTime;

        logger.info("Duration: " + totalTime / 1000.0 + " seconds");
        logger.info("Bye 👋");
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
                if (tx != null) {
                    tx.setGlobalStep(stepCounter.incrementAndGet());
                }
            } catch (InterruptedException e) {
                throw new NoSuchElementException();
            }
        }
        return tx;
    }

    @Override
    public void remove() {
        logger.error("remove() method is unsupported");
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
                    logger.error("interrupted while adding tx to queue, skipping.", e);
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
            final String msg = "cannot stop simulation when state isn't running";
            logger.warn(msg);
            throw new IllegalStateException(msg);
        }

        // XXX: a sloppy drain implementation...bad idea?
        queue.clear();
    }

    @Override
    public boolean onStep(long stepNum) {
        return running.get();
    }
}
