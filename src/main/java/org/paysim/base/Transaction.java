package org.paysim.base;

import org.paysim.actors.SuperActor;
import org.paysim.output.Output;

import java.io.Serializable;
import java.util.ArrayList;

public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int step;
    private int globalStep = -1;
    private final String action;
    private final double amount;

    private final String idOrig;
    private final String nameOrig;
    private final SuperActor.Type typeOrig;
    private final double oldBalanceOrig, newBalanceOrig;

    private final String idDest;
    private final String nameDest;
    private final SuperActor.Type typeDest;
    private final double oldBalanceDest, newBalanceDest;

    private boolean isFraud = false;
    private boolean isFlaggedFraud = false;
    private boolean isUnauthorizedOverdraft = false;
    private boolean isSuccessful = false;

    /**
     * Record pertinent details of a financial transaction, getting values from an originator and a destination.
     *
     * Implemented in such a way as to not purposely keep references to {SuperActor} instances since it's possible
     * a large quantity of Transactions may be kept in memory/on-heap at a time.
     *
     * @param step
     * @param action
     * @param amount
     * @param originator
     * @param oldBalanceOrig
     * @param newBalanceOrig
     * @param destination
     * @param oldBalanceDest
     * @param newBalanceDest
     */
    public Transaction(int step, String action, double amount, SuperActor originator, double oldBalanceOrig,
                       double newBalanceOrig, SuperActor destination, double oldBalanceDest, double newBalanceDest) {
        this.step = step;
        this.action = action;
        this.amount = amount;

        this.idOrig = originator.getId();
        this.nameOrig = originator.getName();
        this.typeOrig = originator.getType();
        this.oldBalanceOrig = oldBalanceOrig;
        this.newBalanceOrig = newBalanceOrig;

        this.idDest = destination.getId();
        this.nameDest = destination.getName();
        this.typeDest = destination.getType();
        this.oldBalanceDest = oldBalanceDest;
        this.newBalanceDest = newBalanceDest;
    }

    public boolean isFailedTransaction(){
        return isFlaggedFraud || isUnauthorizedOverdraft;
    }

    public void setFlaggedFraud(boolean isFlaggedFraud) {
        this.isFlaggedFraud = isFlaggedFraud;
    }

    public void setFraud(boolean isFraud) {
        this.isFraud = isFraud;
    }

    public void setUnauthorizedOverdraft(boolean isUnauthorizedOverdraft) {
        this.isUnauthorizedOverdraft = isUnauthorizedOverdraft;
    }

    public boolean isFlaggedFraud() {
        return isFlaggedFraud;
    }

    public boolean isFraud() {
        return isFraud;
    }

    public int getGlobalStep() {
        return globalStep;
    }

    public void setGlobalStep(int globalStep) {
        this.globalStep = globalStep;
    }

    public int getStep() {
        return step;
    }

    public String getAction() {
        return action;
    }

    public double getAmount() {
        return amount;
    }

    public String getIdOrig() {
        return idOrig;
    }

    public String getNameOrig() {
        return nameOrig;
    }

    public double getOldBalanceOrig() {
        return oldBalanceOrig;
    }

    public double getNewBalanceOrig() {
        return newBalanceOrig;
    }

    public String getIdDest() {
        return idDest;
    }

    public String getNameDest() {
        return nameDest;
    }

    public double getOldBalanceDest() {
        return oldBalanceDest;
    }

    public double getNewBalanceDest() {
        return newBalanceDest;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public SuperActor.Type getOrigType() {
        return typeOrig;
    }

    public SuperActor.Type getDestType() {
        return typeDest;
    }

    @Override
    public String toString(){
        ArrayList<String> properties = new ArrayList<>();

        properties.add(String.valueOf(step));
        properties.add(action);
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, amount));
        properties.add(idOrig);
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, oldBalanceOrig));
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, newBalanceOrig));
        properties.add(idDest);
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, oldBalanceDest));
        properties.add(Output.fastFormatDouble(Output.PRECISION_OUTPUT, newBalanceDest));
        properties.add(Output.formatBoolean(isFraud));
        properties.add(Output.formatBoolean(isFlaggedFraud));
        properties.add(Output.formatBoolean(isUnauthorizedOverdraft));
        properties.add(Output.formatBoolean(isSuccessful));

        return String.join(Output.OUTPUT_SEPARATOR, properties);
    }
}
