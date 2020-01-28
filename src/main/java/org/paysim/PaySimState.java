package org.paysim;

import com.devskiller.jfairy.Bootstrap;
import com.devskiller.jfairy.Fairy;
import ec.util.MersenneTwisterFast;
import org.paysim.actors.*;
import org.paysim.base.ClientActionProfile;
import org.paysim.base.StepActionProfile;
import org.paysim.base.Transaction;
import org.paysim.parameters.ActionTypes;
import org.paysim.parameters.BalancesClients;
import org.paysim.parameters.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sim.engine.SimState;

import java.util.*;

public abstract class PaySimState extends SimState {
    public static final double PAYSIM_VERSION = 2.0;

    private final Logger logger = LoggerFactory.getLogger(PaySimState.class);
    protected Parameters parameters;

    protected List<Client> clients = new ArrayList<>();
    protected List<Merchant> merchants = new ArrayList<>();
    protected List<SuperActor> fraudsters = new ArrayList<>();
    protected List<Bank> banks = new ArrayList<>();

    protected Set<String> bankIds = new HashSet<>();
    protected Set<String> merchantIds = new HashSet<>();
    protected Set<String> clientIds = new HashSet<>();

    protected Map<ClientActionProfile, Integer> countProfileAssignment = new HashMap<>();

    protected Bootstrap.Builder builder;

    long currentStep = 0;

    public PaySimState(Parameters parameters) {
        super(parameters.seed);
        this.parameters = parameters;
        BalancesClients.setRandom(super.random);
        parameters.clientsProfiles.setRandom(super.random);

        // XXX Fairy builder's RandomGenerator can only use int seeds.
        int fairySeed = Math.toIntExact(super.seed());
        builder = Bootstrap.builder()
                .withRandomSeed(fairySeed)
                .withLocale(Locale.US)
                .withLocale(Locale.CANADA)
                .withLocale(Locale.UK);
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
        final int numMerchants = (int) (parameters.nbMerchants * parameters.multiplier);
        logger.info("NbMerchants: " + numMerchants);
        for (int i = 0; i < numMerchants; i++) {
            String name = builder.build().company().getName();
            merchants.add(new Merchant(generateUniqueMerchantId(), name, this.getParameters()));
        }

        // We take a sample of the merchant population and set some as "high risk" (2% arbitrarily)
        List<Merchant> highRiskMerchants = new ArrayList<>();
        for (int i = 0; i < numMerchants / 50; i++) {
            highRiskMerchants.add(merchants.get(random.nextInt(numMerchants)));
        }

        // Fraudsters...
        final int numFraudsters = (int) (parameters.nbFraudsters * parameters.multiplier);
        logger.info("NbFraudsters: " + numFraudsters);

        //Add the 3rd Party fraudsters
        final int num3rdPartyFraudsters = numFraudsters / 2;
        for (int i = 0; i < num3rdPartyFraudsters; i++) {
            String name = builder.build().person().getFullName();
            ThirdPartyFraudster f = new ThirdPartyFraudster(generateUniqueClientId(), name, parameters);

            // 3rd Party Fraudsters select some "favorites" of the high-risk merchants. A Fraudster will have
            // som probability of targeting clients that used these merchants. The remaining events are random
            // client targets from the universe
            f.addFavoredMerchant(highRiskMerchants.get(random.nextInt(highRiskMerchants.size())));
            f.addFavoredMerchant(highRiskMerchants.get(random.nextInt(highRiskMerchants.size())));

            fraudsters.add(f);
            schedule.scheduleRepeating(f);
        }

        //Add the 1st Party fraudsters
        for (int i = 0; i < numFraudsters - num3rdPartyFraudsters; i++) {
            FirstPartyFraudster f = new FirstPartyFraudster(this, random.nextInt(6));
            fraudsters.add(f);
            schedule.scheduleRepeating(f);
        }

        //Add the banks
        logger.info("NbBanks: " + parameters.nbBanks);
        for (int i = 0; i < parameters.nbBanks; i++) {
            String name = String.format("Bank of %s", builder.build().person().getLastName());
            Bank b = new Bank(generateUniqueBankId(), name, this.getParameters());
            banks.add(b);
        }

        //Add the clients
        final int numClients = (int) (parameters.nbClients * parameters.multiplier);
        logger.info("NbClients: " + numClients);
        for (int i = 0; i < numClients; i++) {
            Client c = new Client(this);
            clients.add(c);
        }

        // XXX: Disable drug network for now
        //NetworkDrug.createNetwork(this, parameters.typologiesFolder + TypologiesFiles.drugNetworkOne);

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

    public String generateUniqueBankId() {
        String vat = builder.build().company().getVatIdentificationNumber();

        while (!bankIds.add(vat)) {
            vat = builder.build().company().getVatIdentificationNumber();
        }
        return vat;
    }

    public String generateUniqueMerchantId() {
        String vat = builder.build().company().getVatIdentificationNumber();

        while (!merchantIds.add(vat)) {
            vat = builder.build().company().getVatIdentificationNumber();
        }
        return vat;
    }

    public String generateUniqueClientId() {
        String id = generateId();
        while (!clientIds.add(id)) {
            id = generateId();
        }
        return id;
    }

    public Fairy generateIdentity() {
        return builder.build();
    }

    public String generateEmail() {
        return builder.build().person().getEmail();
    }

    public String generatePhone() {
        return builder.build().person().getTelephoneNumber();
    }

    public String generateClientName() {
        return builder.build().person().getFullName();
    }

    public Merchant pickRandomMerchant() {
        return merchants.get(random.nextInt(merchants.size()));
    }

    public Bank pickRandomBank() {
        return banks.get(random.nextInt(banks.size()));
    }

    public Client pickRandomClient(String originatingId) {
        Client clientDest = null;

        String nameDest = originatingId;
        while (originatingId.equals(nameDest)) {
            clientDest = clients.get(random.nextInt(clients.size()));
            nameDest = clientDest.getId();
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

    public List<Merchant> getMerchants() {
        return merchants;
    }

    public List<SuperActor> getFraudsters() {
        return fraudsters;
    }

    public List<Bank> getBanks() {
        return banks;
    }

    public List<Client> getClients() {
        return clients;
    }

    public Set<String> getClientIds() {
        return clientIds;
    }

    public void addClient(Client c) {
        clients.add(c);
    }

    public Parameters getParameters() {
        return parameters;
    }
}
