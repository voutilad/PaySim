package org.paysim.identity;

import com.opencsv.bean.CsvBindByName;

import java.util.HashMap;
import java.util.Map;

public class MerchantIdentity extends Identity {

    @CsvBindByName
    private boolean highRisk;

    protected MerchantIdentity(String id, String name, boolean isHighRisk) {
        super(id, name);
        this.highRisk = isHighRisk;
    }

    protected MerchantIdentity(String id, String name) {
        this(id, name, false);
    }

    public boolean isHighRisk() {
        return highRisk;
    }

    public void setHighRisk(boolean highRisk) {
        this.highRisk = highRisk;
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(Properties.NAME, name);
        map.put(Properties.ID, id);
        map.put(Properties.HIGH_RISK, highRisk);
        return map;
    }
}
