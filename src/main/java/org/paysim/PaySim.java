package org.paysim;

import org.paysim.base.Transaction;
import org.paysim.output.Output;
import org.paysim.parameters.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is the original PaySim class from the parent project.
 *
 * In general, it's logic is retained unchanged.
 */
public class PaySim extends PaySimState {

    private static final String[] DEFAULT_ARGS = new String[]{"", "-file", "PaySim.properties", "1"};

    public final String simulationName;
    private int totalTransactionsMade = 0;
    private int stepParticipated = 0;

    private List<Transaction> transactions = new ArrayList<>();
    private int currentStep;

    private final Logger logger = LoggerFactory.getLogger(PaySim.class);

    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(PaySim.class);
        logger.info(String.format("PAYSIM: Financial Simulator v%s", PAYSIM_VERSION));
        if (args.length < 4) {
            args = DEFAULT_ARGS;
        }
        int nbTimesRepeat = Integer.parseInt(args[3]);
        String propertiesFile = "";
        for (int x = 0; x < args.length - 1; x++) {
            if (args[x].equals("-file")) {
                propertiesFile = args[x + 1];
            }
        }
        Parameters parameters = new Parameters(propertiesFile);
        for (int i = 0; i < nbTimesRepeat; i++) {
            PaySim p = new PaySim(parameters);
            p.run();
        }
    }

    public PaySim(Parameters parameters) {
        super(parameters);

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date currentTime = new Date();
        simulationName = "PS_" + dateFormat.format(currentTime) + "_" + seed();

        File simulationFolder = new File(parameters.outputPath + simulationName);
        simulationFolder.mkdirs();

        Output.initOutputFilenames(simulationName, parameters.outputPath);
        Output.writeParameters(parameters);
    }

    @Override
    public boolean onTransactions(List<Transaction> transactions) {
        return this.transactions.addAll(transactions);
    }

    @Override
    protected boolean onStep(long stepNum) {
        if (stepNum > Integer.MAX_VALUE) {
            logger.error(String.format("Reached MAX_INT number of steps (%d). Aborting.", Integer.MAX_VALUE));
            return false;
        }
        currentStep = (int) stepNum + 1;
        writeOutputStep();

        if (stepNum % 100 == 100 - 1) {
            logger.info("Step " + currentStep);
        }

        return true;
    }

    @Override
    public void run() {
        logger.info("Starting PaySim Running for " + parameters.nbSteps + " steps.");
        long startTime = System.currentTimeMillis();

        runSimulation();

        logger.info("Finished running " + currentStep + " steps ");
        finish();

        double total = System.currentTimeMillis() - startTime;
        total = total / 1000 / 60;
        logger.info("It took: " + total + " minutes to execute the simulation");
        logger.info("Simulation name: " + simulationName);
    }

    public void finish() {
        Output.writeFraudsters(fraudsters);
        Output.writeClientsProfiles(countProfileAssignment, (int) (parameters.nbClients * parameters.multiplier));
        Output.writeSummarySimulation(this);
    }

    private void resetVariables() {
        if (transactions.size() > 0) {
            stepParticipated++;
        }
        transactions = new ArrayList<>();
    }

    private void writeOutputStep() {
        List<Transaction> transactions = getTransactions();

        totalTransactionsMade += transactions.size();

        Output.incrementalWriteRawLog(currentStep, transactions);
        if (parameters.saveToDB) {
            Output.writeDatabaseLog(parameters.dbUrl, parameters.dbUser, parameters.dbPassword, transactions, simulationName);
        }

        Output.incrementalWriteStepAggregate(currentStep, transactions);
        resetVariables();
    }

    public int getTotalTransactions() {
        return totalTransactionsMade;
    }

    public int getStepParticipated() {
        return stepParticipated;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}