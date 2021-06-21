package org.paysim.identity;

import com.opencsv.bean.CsvBindByName;

import java.util.HashMap;
import java.util.Map;

public class MerchantIdentity extends Identity {
    private static final String MERCHANT_IDENTIFIER = "M";

    @CsvBindByName
    private boolean isHighRisk;

    protected MerchantIdentity(String id, String name, boolean isHighRisk) {
        super(id, MERCHANT_IDENTIFIER + name);
        this.isHighRisk = isHighRisk;
    }

    protected MerchantIdentity(String id, String name) {
        this(id, name, false);
    }

    public boolean isHighRisk() {
        return isHighRisk;
    }

    public void setHighRisk(boolean highRisk) {
        isHighRisk = highRisk;
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(Properties.NAME, name);
        map.put(Properties.ID, id);
        map.put(Properties.HIGH_RISK, isHighRisk);
        return map;
    }
}
