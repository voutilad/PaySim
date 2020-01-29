package org.paysim.identity;

/**
 * This is stupid...but instead of managing the abstraction of fraudsters from a base Client,
 * let's just use a dumb 1-method interface for now.
 */
public interface HasClientIdentity {
    public ClientIdentity getClientIdentity();
}
