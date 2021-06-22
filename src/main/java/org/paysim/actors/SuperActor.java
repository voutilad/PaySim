package org.paysim.actors;

import com.opencsv.bean.CsvBindByName;
import org.paysim.PaySimState;
import org.paysim.identity.Identifiable;
import org.paysim.parameters.Parameters;
import org.paysim.utils.BoundedArrayDeque;

import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public abstract class SuperActor implements Identifiable {
    protected final Deque<Client> prevInteractions;
    protected final Parameters parameters;

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
