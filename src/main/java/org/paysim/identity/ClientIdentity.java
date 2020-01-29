package org.paysim.identity;

import org.paysim.actors.Properties;

import java.util.HashMap;
import java.util.Map;

public class ClientIdentity extends Identity {
    public final String email;
    public final String ssn;
    public final String phoneNumber;

    protected ClientIdentity(String id, String name, String email, String ssn, String phoneNumber) {
        super(id, name);
        this.email = email;
        this.ssn = ssn;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<>();
        map.put(Properties.NAME, name);
        map.put(Properties.EMAIL, email);
        map.put(Properties.PHONE, phoneNumber);
        map.put(Properties.SSN, ssn);
        map.put(Properties.CCN, id);

        return map;
    }
}
