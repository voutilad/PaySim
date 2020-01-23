package org.paysim.actors;

import org.paysim.parameters.Parameters;

public class Bank extends SuperActor {
    private static final String BANK_IDENTIFIER = "B";

    public Bank(String id, String name, Parameters parameters) {
        super(BANK_IDENTIFIER + id, name, parameters);
    }

    @Override
    public Type getType() {
        return Type.BANK;
    }
}
