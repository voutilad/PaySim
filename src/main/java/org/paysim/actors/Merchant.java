package org.paysim.actors;

import org.paysim.parameters.Parameters;

public class Merchant extends SuperActor {
    private static final String MERCHANT_IDENTIFIER = "M";

    public Merchant(String id, String name, Parameters parameters) {
        super(MERCHANT_IDENTIFIER + id, name, parameters);
    }

    @Override
    public Type getType() {
        return Type.MERCHANT;
    }
}
