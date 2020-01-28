package org.paysim.identity;

import java.util.Objects;

public class Identity {
    public final String name;
    public final String ssn;
    public final String phoneNumber;

    public Identity(String name, String ssn, String phoneNumber) {
        this.name = name;
        this.ssn = ssn;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identity identity = (Identity) o;
        return Objects.equals(name, identity.name) &&
                Objects.equals(ssn, identity.ssn) &&
                Objects.equals(phoneNumber, identity.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ssn, phoneNumber);
    }
}
