package org.paysim;

import ec.util.MersenneTwisterFast;
import org.paysim.actors.Bank;
import org.paysim.actors.Client;
import org.paysim.actors.Fraudster;
import org.paysim.actors.Merchant;
import org.paysim.actors.networkdrugs.NetworkDrug;
import org.paysim.base.ClientActionProfile;
import org.paysim.base.StepActionProfile;
import org.paysim.base.Transaction;
import org.paysim.parameters.ActionTypes;
import org.paysim.parameters.BalancesClients;
import org.paysim.parameters.Parameters;
import org.paysim.parameters.TypologiesFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sim.engine.SimState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PaySimState extends SimState {
    public static final double PAYSIM_VERSION = 2.0;

    private final Logger logger = LoggerFactory.getLogger(PaySimState.class);
    protected Parameters parameters;

    protected List<Client> clients = new ArrayList<>();
    protected List<Merchant> merchants = new ArrayList<>();
    protected List<Fraudster> fraudsters = new ArrayList<>();
    protected List<Bank> banks = new ArrayList<>();

    protected Map<ClientActionProfile, Integer> countProfileAssignment = new HashMap<>();

    long currentStep = 0;

    public PaySimState(Parameters parameters) {
        super(parameters.seed);
        this.parameters = parameters;
        BalancesClients.setRandom(super.random);
        parameters.clientsProfiles.setRandom(super.random);
    }

    public abstract boolean onTransactions(List<Transaction> transactions);

    protected abstract boolean onStep(long stepNum);

    public abstract void run();

    protected void runSimulation() {
        currentStep = 0;
        super.start();
        initCounters();
        initActors();

        while ((currentStep = schedule.getSteps()) < parameters.nbSteps) {
            if (!schedule.step(this))
                break;
            if (!onStep(currentStep))
                break;
            if (currentStep > Integer.MAX_VALUE) // not supported yet
                break;
        }
        super.finish();
    }

    private void initCounters() {
        for (String action : ActionTypes.getActions()) {
            for (ClientActionProfile clientActionProfile : parameters.clientsProfiles.getProfilesFromAction(action)) {
                countProfileAssignment.put(clientActionProfile, 0);
            }
        }
    }

    private void initActors() {
        logger.info("Init - Seed " + seed());

        //Add the merchants
        logger.info("NbMerchants: " + (int) (parameters.nbMerchants * parameters.multiplier));
        for (int i = 0; i < parameters.nbMerchants * parameters.multiplier; i++) {
            Merchant m = new Merchant(generateId(), this.getParameters());
            merchants.add(m);
        }

        //Add the fraudsters
        logger.info("NbFraudsters: " + (int) (parameters.nbFraudsters * parameters.multiplier));
        for (int i = 0; i < parameters.nbFraudsters * parameters.multiplier; i++) {
            Fraudster f = new Fraudster(generateId(), parameters);
            fraudsters.add(f);
            schedule.scheduleRepeating(f);
        }

        //Add the banks
        logger.info("NbBanks: " + parameters.nbBanks);
        for (int i = 0; i < parameters.nbBanks; i++) {
            Bank b = new Bank(generateId(), this.getParameters());
            banks.add(b);
        }

        //Add the clients
        logger.info("NbClients: " + (int) (parameters.nbClients * parameters.multiplier));
        for (int i = 0; i < parameters.nbClients * parameters.multiplier; i++) {
            Client c = new Client(this);
            clients.add(c);
        }

        NetworkDrug.createNetwork(this, parameters.typologiesFolder + TypologiesFiles.drugNetworkOne);

        // Do not write code under this part otherwise clients will not be used in simulation
        // Schedule clients to act at each step of the simulation
        for (Client c : clients) {
            schedule.scheduleRepeating(c);
        }
    }

    public Map<String, ClientActionProfile> pickNextClientProfile() {
        Map<String, ClientActionProfile> profile = new HashMap<>();
        for (String action : ActionTypes.getActions()) {
            ClientActionProfile clientActionProfile = parameters.clientsProfiles.pickNextActionProfile(action);

            profile.put(action, clientActionProfile);

            int count = countProfileAssignment.get(clientActionProfile);
            countProfileAssignment.put(clientActionProfile, count + 1);
        }
        return profile;
    }

    public MersenneTwisterFast getRNG() {
        return super.random;
    }

    public String generateId() {
        final String alphabet = "0123456789";
        final int sizeId = 10;
        StringBuilder idBuilder = new StringBuilder(sizeId);

        for (int i = 0; i < sizeId; i++)
            idBuilder.append(alphabet.charAt(random.nextInt(alphabet.length())));
        return idBuilder.toString();
    }

    public Merchant pickRandomMerchant() {
        return merchants.get(random.nextInt(merchants.size()));
    }

    public Bank pickRandomBank() {
        return banks.get(random.nextInt(banks.size()));
    }

    public Client pickRandomClient(String nameOrig) {
        Client clientDest = null;

        String nameDest = nameOrig;
        while (nameOrig.equals(nameDest)) {
            clientDest = clients.get(random.nextInt(clients.size()));
            nameDest = clientDest.getName();
        }
        return clientDest;
    }

    // XXX: The next few methods fudge {currentStep} to an int for now,
    //      manually asserting in runSimulation()
    public int getStepTargetCount() {
        return parameters.stepsProfiles.getTargetCount((int) currentStep);
    }

    public Map<String, Double> getStepProbabilities() {
        return parameters.stepsProfiles.getProbabilitiesPerStep((int) currentStep);
    }

    public StepActionProfile getStepAction(String action) {
        return parameters.stepsProfiles.getActionForStep((int) currentStep, action);
    }

    public List<Client> getClients() {
        return clients;
    }

    public void addClient(Client c) {
        clients.add(c);
    }

    public Parameters getParameters() {
        return parameters;
    }
}
