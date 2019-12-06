package org.paysim.actors;

import org.paysim.parameters.Parameters;

public abstract class SuperActor {
    protected final Parameters parameters;
    private final String name;
    private boolean isFraud = false;
    double balance = 0;
    double overdraftLimit;

    public enum Type {
        BANK,
        CLIENT,
        FRAUDSTER,
        MERCHANT,
        MULE
    }

    SuperActor(String name, Parameters parameters) {
        this.name = name;
        this.parameters = parameters;
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

    public String getName() {
        return name;
    }

    public abstract Type getType();

    @Override
    public String toString() {
        return String.format("%s [%s]", name, getType());
    }
}
