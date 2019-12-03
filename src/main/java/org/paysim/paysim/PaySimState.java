package org.paysim.paysim;

import ec.util.MersenneTwisterFast;
import org.paysim.paysim.actors.Bank;
import org.paysim.paysim.actors.Client;
import org.paysim.paysim.actors.Fraudster;
import org.paysim.paysim.actors.Merchant;
import org.paysim.paysim.actors.networkdrugs.NetworkDrug;
import org.paysim.paysim.base.ClientActionProfile;
import org.paysim.paysim.base.StepActionProfile;
import org.paysim.paysim.base.Transaction;
import org.paysim.paysim.parameters.ActionTypes;
import org.paysim.paysim.parameters.BalancesClients;
import org.paysim.paysim.parameters.Parameters;
import org.paysim.paysim.parameters.TypologiesFiles;
import sim.engine.SimState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PaySimState extends SimState {
    public static final double PAYSIM_VERSION = 2.0;

    protected ArrayList<Client> clients = new ArrayList<>();
    protected ArrayList<Merchant> merchants = new ArrayList<>();
    protected ArrayList<Fraudster> fraudsters = new ArrayList<>();
    protected ArrayList<Bank> banks = new ArrayList<>();

    protected Map<ClientActionProfile, Integer> countProfileAssignment = new HashMap<>();

    long currentStep = 0;

    public PaySimState(long seed) {
        super(seed);
        BalancesClients.setRandom(super.random);
        Parameters.clientsProfiles.setRandom(super.random);
    }

    public abstract void onTransactions(List<Transaction> transactions);

    protected abstract boolean onStep(long stepNum);

    protected void runSimulation() {
        currentStep = 0;
        super.start();
        initCounters();
        initActors();

        while ((currentStep = schedule.getSteps()) < Parameters.nbSteps) {
            if (!schedule.step(this))
                break;
            if (!onStep(currentStep))
                break;
            if (currentStep > Integer.MAX_VALUE) // not supported yet
                break;
        }
    }

    private void initCounters() {
        for (String action : ActionTypes.getActions()) {
            for (ClientActionProfile clientActionProfile : Parameters.clientsProfiles.getProfilesFromAction(action)) {
                countProfileAssignment.put(clientActionProfile, 0);
            }
        }
    }

    private void initActors() {
        System.out.println("Init - Seed " + seed());

        //Add the merchants
        System.out.println("NbMerchants: " + (int) (Parameters.nbMerchants * Parameters.multiplier));
        for (int i = 0; i < Parameters.nbMerchants * Parameters.multiplier; i++) {
            Merchant m = new Merchant(generateId());
            merchants.add(m);
        }

        //Add the fraudsters
        System.out.println("NbFraudsters: " + (int) (Parameters.nbFraudsters * Parameters.multiplier));
        for (int i = 0; i < Parameters.nbFraudsters * Parameters.multiplier; i++) {
            Fraudster f = new Fraudster(generateId());
            fraudsters.add(f);
            schedule.scheduleRepeating(f);
        }

        //Add the banks
        System.out.println("NbBanks: " + Parameters.nbBanks);
        for (int i = 0; i < Parameters.nbBanks; i++) {
            Bank b = new Bank(generateId());
            banks.add(b);
        }

        //Add the clients
        System.out.println("NbClients: " + (int) (Parameters.nbClients * Parameters.multiplier));
        for (int i = 0; i < Parameters.nbClients * Parameters.multiplier; i++) {
            Client c = new Client(this);
            clients.add(c);
        }

        NetworkDrug.createNetwork(this, Parameters.typologiesFolder + TypologiesFiles.drugNetworkOne);

        // Do not write code under this part otherwise clients will not be used in simulation
        // Schedule clients to act at each step of the simulation
        for (Client c : clients) {
            schedule.scheduleRepeating(c);
        }
    }

    public Map<String, ClientActionProfile> pickNextClientProfile() {
        Map<String, ClientActionProfile> profile = new HashMap<>();
        for (String action : ActionTypes.getActions()) {
            ClientActionProfile clientActionProfile = Parameters.clientsProfiles.pickNextActionProfile(action);

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
        return Parameters.stepsProfiles.getTargetCount((int) currentStep);
    }

    public Map<String, Double> getStepProbabilities() {
        return Parameters.stepsProfiles.getProbabilitiesPerStep((int) currentStep);
    }

    public StepActionProfile getStepAction(String action) {
        return Parameters.stepsProfiles.getActionForStep((int) currentStep, action);
    }

    public ArrayList<Client> getClients() {
        return clients;
    }

    public void addClient(Client c) {
        clients.add(c);
    }

}
