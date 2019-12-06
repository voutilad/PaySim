package org.paysim.actors;

import org.paysim.parameters.Parameters;

public class Merchant extends SuperActor {
    private static final String MERCHANT_IDENTIFIER = "M";

    public Merchant(String name, Parameters parameters) {
        super(MERCHANT_IDENTIFIER + name, parameters);
    }

    @Override
    public Type getType() {
        return Type.MERCHANT;
    }
}
