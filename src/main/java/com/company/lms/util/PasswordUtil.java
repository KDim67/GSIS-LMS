package com.company.lms.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtil {

    private static final int COST = 12;

    private PasswordUtil() {
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.withDefaults().hashToString(COST, plainPassword.toCharArray());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }

        return BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword).verified;
    }
}