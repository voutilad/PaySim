package org.paysim.identity;

import java.util.HashMap;
import java.util.Map;

public class BankIdentity extends Identity {
    private static final String BANK_IDENTIFIER = "B";

    public BankIdentity(String id, String name) {
        super(id, BANK_IDENTIFIER + name);
    }

    @Override
    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<>();
        map.put(Properties.NAME, name);
        return map;
    }
}
