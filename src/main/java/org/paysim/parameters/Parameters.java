package org.paysim.parameters;

import java.io.FileInputStream;
import java.util.Properties;


public class Parameters {
    private static String seedString;
    public final int seed;
    public final int nbClients, nbMerchants, nbBanks, nbFraudsters, nbSteps;
    public final double multiplier, fraudProbability, transferLimit;
    public final String aggregatedTransactions, maxOccurrencesPerClient, initialBalancesDistribution,
            overdraftLimits, clientsProfilesFile, transactionsTypes;
    public final String typologiesFolder, outputPath;
    public final boolean saveToDB;
    public final String dbUrl, dbUser, dbPassword;

    public final StepsProfiles stepsProfiles;
    public final ClientsProfiles clientsProfiles;

    public Parameters(String propertiesFile) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(propertiesFile));
        } catch (Exception e) {
            // TODO: refactor to throw exception
            e.printStackTrace();
        }

        seedString = String.valueOf(props.getProperty("seed"));
        seed = parseSeed(seedString);
        nbSteps = Integer.parseInt(props.getProperty("nbSteps"));
        multiplier = Double.parseDouble(props.getProperty("multiplier"));

        nbClients = Integer.parseInt(props.getProperty("nbClients"));
        nbFraudsters = Integer.parseInt(props.getProperty("nbFraudsters"));
        nbMerchants = Integer.parseInt(props.getProperty("nbMerchants"));
        nbBanks = Integer.parseInt(props.getProperty("nbBanks"));

        fraudProbability = Double.parseDouble(props.getProperty("fraudProbability"));
        transferLimit = Double.parseDouble(props.getProperty("transferLimit"));

        transactionsTypes = props.getProperty("transactionsTypes");
        aggregatedTransactions = props.getProperty("aggregatedTransactions");
        maxOccurrencesPerClient = props.getProperty("maxOccurrencesPerClient");
        initialBalancesDistribution = props.getProperty("initialBalancesDistribution");
        overdraftLimits = props.getProperty("overdraftLimits");
        clientsProfilesFile = props.getProperty("clientsProfiles");

        typologiesFolder = props.getProperty("typologiesFolder");
        outputPath = props.getProperty("outputPath");

        saveToDB = props.getProperty("saveToDB").equals("1");
        dbUrl = props.getProperty("dbUrl");
        dbUser = props.getProperty("dbUser");
        dbPassword = props.getProperty("dbPassword");

        ActionTypes.loadActionTypes(transactionsTypes);
        BalancesClients.initBalanceClients(initialBalancesDistribution);
        BalancesClients.initOverdraftLimits(overdraftLimits);
        clientsProfiles = new ClientsProfiles(clientsProfilesFile);
        stepsProfiles = new StepsProfiles(aggregatedTransactions, multiplier, nbSteps);
        ActionTypes.loadMaxOccurrencesPerClient(maxOccurrencesPerClient);
    }

    private int parseSeed(String seedString) {
        // /!\ MASON seed is using an int internally
        // https://github.com/eclab/mason/blob/66d38fa58fae3e250b89cf6f31bcfa9d124ffd41/mason/sim/engine/SimState.java#L45
        if (seedString.equals("time")) {
            return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
        } else {
            return Integer.parseInt(seedString);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("seed=" + seed + System.lineSeparator());
        sb.append("nbSteps=" + nbSteps + System.lineSeparator());
        sb.append("multiplier=" + multiplier + System.lineSeparator());
        sb.append("nbFraudsters=" + nbFraudsters + System.lineSeparator());
        sb.append("nbMerchants=" + nbMerchants + System.lineSeparator());
        sb.append("fraudProbability=" + fraudProbability + System.lineSeparator());
        sb.append("transferLimit=" + transferLimit + System.lineSeparator());
        sb.append("transactionsTypes=" + transactionsTypes + System.lineSeparator());
        sb.append("aggregatedTransactions=" + aggregatedTransactions + System.lineSeparator());
        sb.append("clientsProfilesFile=" + clientsProfilesFile + System.lineSeparator());
        sb.append("initialBalancesDistribution=" + initialBalancesDistribution + System.lineSeparator());
        sb.append("maxOccurrencesPerClient=" + maxOccurrencesPerClient + System.lineSeparator());
        sb.append("outputPath=" + outputPath + System.lineSeparator());
        sb.append("saveToDB=" + saveToDB + System.lineSeparator());
        sb.append("dbUrl=" + dbUrl + System.lineSeparator());
        sb.append("dbUser=" + dbUser + System.lineSeparator());
        sb.append("dbPassword=" + dbPassword + System.lineSeparator());
        return sb.toString();
    }
}
