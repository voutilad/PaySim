package org.paysim.actors;

import org.paysim.parameters.Parameters;

public class Bank extends SuperActor {
    private static final String BANK_IDENTIFIER = "B";

    public Bank(String name, Parameters parameters) {
        super(BANK_IDENTIFIER + name, parameters);
    }

    @Override
    public Type getType() {
        return Type.BANK;
    }
}
