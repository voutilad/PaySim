package org.paysim.identity;

import org.paysim.actors.Properties;

import java.util.HashMap;
import java.util.Map;

public class MerchantIdentity extends Identity {
    private static final String MERCHANT_IDENTIFIER = "M";

    protected MerchantIdentity(String id, String name) {
        super(id, MERCHANT_IDENTIFIER + name);
    }

    @Override
    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<>();
        map.put(Properties.NAME, name);
        map.put(Properties.ID, id);
        return map;
    }
}
