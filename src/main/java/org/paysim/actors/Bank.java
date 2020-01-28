package org.paysim.actors;

import org.paysim.PaySimState;

public class Bank extends SuperActor {
    private static final String BANK_IDENTIFIER = "B";

    public Bank(String id, String name, PaySimState state) {
        super(BANK_IDENTIFIER + id, state);
        properties.put(Properties.NAME, name);
    }

    @Override
    public Type getType() {
        return Type.BANK;
    }
}
