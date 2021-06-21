package org.paysim.parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;


public class Parameters {
    private final Logger logger = LoggerFactory.getLogger(Parameters.class);

    private static String seedString;
    public final int seed;
    public final int nbClients, nbMerchants, nbBanks, nbFraudsters, nbSteps;
    public final double multiplier, transferLimit;
    public final float thirdPartyNewVictimProbability;
    public final double firstPartyFraudProbability, clientReuseProbability,
            clientAcquaintanceProbability, merchantReuseProbability,
            thirdPartyFraudProbability, thirdPartyPercentHighRiskMerchants;
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
            logger.error(String.format("error loading properties file: %s", propertiesFile), e);
        }

        seedString = String.valueOf(props.getProperty("seed"));
        seed = parseSeed(seedString);
        nbSteps = Integer.parseInt(props.getProperty("nbSteps"));
        multiplier = Double.parseDouble(props.getProperty("multiplier"));

        nbClients = Integer.parseInt(props.getProperty("nbClients"));
        nbFraudsters = Integer.parseInt(props.getProperty("nbFraudsters"));
        nbMerchants = Integer.parseInt(props.getProperty("nbMerchants"));
        nbBanks = Integer.parseInt(props.getProperty("nbBanks"));

        // Support older props files for now until I can migrate them
        if (props.get("fraudProbability") != null) {
            double probability = Double.parseDouble(props.getProperty("fraudProbability"));
            logger.warn("HEADS UP! the 'fraudProbability' property is deprecated, but was found in your properties file");
            logger.warn("PaySim will set both 1st and 3rd party fraud probability to the value of 'fraudProbability' ({})", probability);
            firstPartyFraudProbability = probability;
            thirdPartyFraudProbability = probability;
        } else {
            firstPartyFraudProbability = Double.parseDouble(props.getProperty("firstPartyFraudProbability"));
            thirdPartyFraudProbability = Double.parseDouble(props.getProperty("thirdPartyFraudProbability"));
        }

        // XXX: 0.9 based on some Python simulations. May change this later.
        merchantReuseProbability = Double.parseDouble(props.getProperty("merchantReuseProbability", "0.9"));

        // XXX: wags
        clientReuseProbability = Double.parseDouble(props.getProperty("clientReuseProbability", "0.9"));
        clientAcquaintanceProbability = Double.parseDouble(props.getProperty("clientAcquaintanceProbability", "0.9"));

        String nvp = props.getProperty("thirdPartyNewVictimProbability");
        if (nvp == null || nvp.equals("0.4")) {
            thirdPartyNewVictimProbability = 0.4f;
        } else {
            thirdPartyNewVictimProbability =
                    Float.parseFloat(props.getProperty("thirdPartyNewVictimProbability", "0.4"));
        }
        thirdPartyPercentHighRiskMerchants = Double.parseDouble(props.getProperty("thirdPartyPercentHighRiskMerchants", "0.02"));
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
        sb.append("firstPartyFraudProbability=" + firstPartyFraudProbability + System.lineSeparator());
        sb.append("merchantReuseProbability=" + merchantReuseProbability + System.lineSeparator());
        sb.append("thirdPartyFraudProbability=" + thirdPartyFraudProbability + System.lineSeparator());
        sb.append("thirdPartyNewVictimProbability=" + thirdPartyNewVictimProbability + System.lineSeparator());
        sb.append("thirdPartyPercentHighRiskMerchants=" + thirdPartyPercentHighRiskMerchants + System.lineSeparator());
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
