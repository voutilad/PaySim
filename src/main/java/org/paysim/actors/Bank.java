package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.identity.BankIdentity;
import org.paysim.identity.Identity;

import java.util.Map;

public class Bank extends SuperActor {

    private final BankIdentity identity;

    public Bank(PaySimState state, BankIdentity identity) {
        super(state);
        this.identity = identity;
    }

    @Override
    public Type getType() {
        return Type.BANK;
    }

    @Override
    public String getId() {
        return identity.id;
    }

    @Override
    public String getName() {
        return identity.name;
    }

    @Override
    public Identity getIdentity() {
        return identity;
    }

    @Override
    public Map<String, String> getIdentityAsMap() {
        return identity.asMap();
    }
}
