package org.paysim.identity;

import com.opencsv.bean.CsvBindByName;

import java.util.HashMap;
import java.util.Map;

public class ClientIdentity extends Identity {

    @CsvBindByName
    public final String email;
    @CsvBindByName
    public final String ssn;
    @CsvBindByName
    public final String phoneNumber;

    protected ClientIdentity(String id, String name, String email, String ssn, String phoneNumber) {
        super(id, name);
        this.email = email;
        this.ssn = ssn;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Creates a new ClientIdentity from this instance, replacing the given property with the provided Value.
     * <p>
     * TODO: candidate for refactor of Properties into enum instead of Strings.
     *
     * @param property String name of property (See @{org.paysim.identity.Properties}
     * @param value    String value to use in place of old property value
     * @return new ClientIdentity, copying
     */
    public ClientIdentity replaceProperty(String property, String value) {
        switch (property) {
            case Properties.EMAIL:
                return new ClientIdentity(id, name, value, ssn, phoneNumber);
            case Properties.SSN:
                return new ClientIdentity(id, name, email, value, phoneNumber);
            case Properties.PHONE:
                return new ClientIdentity(id, name, email, ssn, value);
            default:
                return this;
        }
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(Properties.NAME, name);
        map.put(Properties.EMAIL, email);
        map.put(Properties.PHONE, phoneNumber);
        map.put(Properties.SSN, ssn);
        map.put(Properties.CCN, id);

        return map;
    }
}
