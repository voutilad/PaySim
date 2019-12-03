package org.paysim.actors;

public class Bank extends SuperActor {
    private static final String BANK_IDENTIFIER = "B";

    public Bank(String name) {
        super(BANK_IDENTIFIER + name);
    }

    @Override
    public Type getType() {
        return Type.BANK;
    }
}
