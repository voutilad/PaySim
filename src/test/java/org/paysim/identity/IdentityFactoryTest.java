package org.paysim.identity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Duration;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class IdentityFactoryTest {

    @Test
    void successiveCallsCreateNewPeople() {
        IdentityFactory factory = new IdentityFactory(1);
        ClientIdentity identity1 = factory.nextPerson();
        ClientIdentity identity2 = factory.nextPerson();
        Assertions.assertNotEquals(identity1, identity2);
        Assertions.assertNotEquals(identity1.name, identity2.name);
        Assertions.assertNotEquals(identity1.id, identity2.id);
    }

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
            int suffix = Integer.valueOf(parts[0].substring(addrlen - 3));
            Assertions.assertTrue(suffix > -1 && suffix < 100,
                    suffix + " should be double digits");
        } catch (Exception e) {
            Assertions.fail("didn't find digits in email: " + email);
        }
    }

    @Test
    void collisionTest() {
        System.out.println("Starting collision test...");
        final long start = System.currentTimeMillis();
        final int max = 1_000_000;
        final AtomicInteger collisions = new AtomicInteger(0);
        final IdentityFactory factory = new IdentityFactory(1);
        final HashSet<String> ccSet = new HashSet<>(max);

        Assertions.assertTimeout(Duration.ofSeconds(15), () -> {
            while (ccSet.size() < max) {
                String cc = factory.getNextCreditCard();
                while (!ccSet.add(cc)) {
                    collisions.incrementAndGet();
                    cc = factory.getNextCreditCard();
                }
            }
        });
        final long finish = System.currentTimeMillis();
        System.out.println("Finished in " + (finish - start) + " millis");
        System.out.println("collisions: " + collisions.get());
    }
}
