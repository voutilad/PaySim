package org.paysim.actors;

import org.paysim.PaySimState;
import org.paysim.identity.Identity;
import org.paysim.identity.MerchantIdentity;

import java.util.Map;

public class Merchant extends SuperActor {
    private final MerchantIdentity identity;

    public Merchant(PaySimState state, MerchantIdentity identity) {
        super(state);
        this.identity = identity;
    }

    @Override
    public Type getType() {
        return Type.MERCHANT;
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
