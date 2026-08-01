package kg.attractor.payment_system.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {

    private static final int ACCOUNT_NUMBER_LENGTH = 16;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        StringBuilder number = new StringBuilder(ACCOUNT_NUMBER_LENGTH);
        number.append(secureRandom.nextInt(9) + 1);

        for (int i = 1; i < ACCOUNT_NUMBER_LENGTH; i++) {
            number.append(secureRandom.nextInt(10));
        }

        return number.toString();
    }
}
