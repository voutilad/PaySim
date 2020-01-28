package org.paysim.actors;

import org.paysim.PaySimState;

public class Merchant extends SuperActor {
    private static final String MERCHANT_IDENTIFIER = "M";

    public Merchant(String id, String name, PaySimState state) {
        super(MERCHANT_IDENTIFIER + id, state);
        properties.put(Properties.NAME, name);
    }

    @Override
    public Type getType() {
        return Type.MERCHANT;
    }
}
