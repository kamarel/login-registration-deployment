package com.mykcc.login_registrations.Security;

import java.security.SecureRandom;

public class TokenUtil {
    private static final SecureRandom secureRandom = new SecureRandom();

    // Generate a secure 6-digit random code
    public static String generateRandomSixDigitCode() {
        int code = 100000 + secureRandom.nextInt(900000); // Generate a number between 100000 and 999999
        return String.valueOf(code);
    }
}