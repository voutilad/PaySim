package org.paysim.identity;

import com.devskiller.jfairy.Bootstrap;
import com.devskiller.jfairy.producer.company.Company;
import com.devskiller.jfairy.producer.person.Person;

import java.util.Locale;

/**
 * Wraps the jFairy library and provides an identity generation function.
 *
 * This keeps some of the jFairy confusion to a minimum.
 */
public class IdentityFactory {
    final private Bootstrap.Builder builder;

    public IdentityFactory(int randomSeed) {
        builder = Bootstrap.builder()
                .withRandomSeed(randomSeed)
                .withLocale(Locale.US)
                .withLocale(Locale.CANADA);
    }

    public Identity nextPerson() {
        Person p = builder.build().person();
        Identity id = new Identity(
                p.getFullName(),
                p.getNationalIdentityCardNumber(),
                p.getTelephoneNumber());

        return id;
    }

    public String getNextVAT() {
        Company c = builder.build().company();
        return c.getVatIdentificationNumber();
    }

    public String getNextCreditCard() {
        return builder.build().creditCard().getCardNumber();
    }

    public String nextMerchantName() {
        Company c = builder.build().company();
        return c.getName();
    }
}
