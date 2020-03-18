package org.paysim.identity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdentityFactoryTest {

    @Test
    void generatedEmailsAddRandomDigits() {
        // Issue #2 - Email generation is rubbish
        // Basically since the name collision rate is so damn high with jFairy, let's insert
        // some digits like someone would in real life, decreasing the rate of duplication
        // for email addresses.

        IdentityFactory factory = new IdentityFactory(1);
        ClientIdentity identity = factory.nextPerson();
        String email = identity.email;
        String[] parts = email.split("@");
        Assertions.assertEquals(2, parts.length);
        int addrlen = parts[0].length();

        try {
            int suffix = Integer.valueOf(parts[0].substring(addrlen - 2));
            Assertions.assertTrue(suffix > -1 && suffix < 100,
                    suffix + " should be double digits");
        } catch (Exception e) {
            Assertions.fail("didn't find digits in email: " + email);
        }
    }
}
