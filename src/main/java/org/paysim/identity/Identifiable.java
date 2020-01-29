package org.paysim.identity;

import java.util.Map;

public interface Identifiable {
    public String getId();
    public String getName();
    public Identity getIdentity();

    public Map<String, String> getIdentityAsMap();
}
