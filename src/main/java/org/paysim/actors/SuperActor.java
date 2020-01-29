package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.identity.Identifiable;
import org.paysim.identity.Identity;
import org.paysim.parameters.Parameters;
import org.paysim.utils.BoundedArrayDeque;

import java.util.*;

public abstract class SuperActor implements Identifiable {
    protected final Deque<Client> prevInteractions;
    protected final Parameters parameters;

    private boolean isFraud = false;
    double balance = 0;
    double overdraftLimit;

    public enum Type {
        BANK,
        CLIENT,
        FIRST_PARTY_FRAUDSTER,
        THIRD_PARTY_FRAUDSTER,
        MERCHANT,
        MULE
    }

    SuperActor(PaySimState state) {
        parameters = state.getParameters();
        prevInteractions = new BoundedArrayDeque<>(100);
    }

    void deposit(double amount) {
        balance += amount;
    }

    boolean withdraw(double amount) {
        boolean unauthorizedOverdraft = false;

        if (balance - amount < overdraftLimit) {
            unauthorizedOverdraft = true;
        } else {
            balance -= amount;
        }

        return unauthorizedOverdraft;
    }

    boolean isFraud() {
        return isFraud;
    }

    void setFraud(boolean isFraud) {
        this.isFraud = isFraud;
    }

    protected double getBalance() {
        return balance;
    }

    public void rememberClient(Client client) {
        prevInteractions.push(client);
    }

    public List<Client> getRecentClients() {
        return Arrays.asList(prevInteractions.toArray(new Client[prevInteractions.size()]));
    }

    public abstract Type getType();

    @Override
    public String toString() {
        return String.format("%s [%s]", getId(), getType());
    }
}
