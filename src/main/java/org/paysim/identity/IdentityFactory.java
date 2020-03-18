package org.paysim.identity;

import com.devskiller.jfairy.Bootstrap;
import com.devskiller.jfairy.producer.company.Company;
import com.devskiller.jfairy.producer.person.Person;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Wraps the jFairy library and provides an identity generation function.
 *
 * This keeps some of the jFairy confusion to a minimum.
 */
public class IdentityFactory {
    private final Set<String> ccnSet = new HashSet<>();

    final private Bootstrap.Builder builder;

    public IdentityFactory(int randomSeed) {
        builder = Bootstrap.builder()
                .withRandomSeed(randomSeed)
                .withLocale(Locale.US)
                .withLocale(Locale.CANADA);
    }

    protected String addSuffixToEmail(String email, String suffix) {
        String[] parts = email.split("@");
        assert parts.length == 2;
        return parts[0] + suffix + "@" + parts[1];
    }

    public ClientIdentity nextPerson() {
        Person p = builder.build().person();

        return new ClientIdentity(
                getNextCreditCard(),
                p.getFullName(),
                addSuffixToEmail(p.getEmail(), p.getNationalIdentityCardNumber().substring(0, 2)),
                p.getNationalIdentityCardNumber(),
                p.getTelephoneNumber());
    }

    public BankIdentity nextBank() {
        Person p = builder.build().person();
        return new BankIdentity(getNextVAT(), String.format("Bank of %s", p.getLastName()));
    }

    public MerchantIdentity nextMerchant() {
        Company c = builder.build().company();
        return new MerchantIdentity(c.getVatIdentificationNumber(), c.getName());
    }

    public String getNextVAT() {
        Company c = builder.build().company();
        return c.getVatIdentificationNumber();
    }

    public String getNextCreditCard() {
        String ccn = builder.build().creditCard().getCardNumber();
        while (!ccnSet.add(ccn)) {
            ccn = builder.build().creditCard().getCardNumber();
        }
        return ccn;
    }

    public String nextMerchantName() {
        Company c = builder.build().company();
        return c.getName();
    }
}
