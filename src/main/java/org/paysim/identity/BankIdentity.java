package org.paysim.identity;

import java.util.HashMap;
import java.util.Map;

public class BankIdentity extends Identity {
    private static final String BANK_IDENTIFIER = "B";

    public BankIdentity(String id, String name) {
        super(BANK_IDENTIFIER + id, name);
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(Properties.NAME, name);
        return map;
    }
}
