package com.micro.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Thin wrapper around BCrypt with configurable work factor.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String hashPassword(String plain, int workFactor) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(workFactor));
    }

    public static boolean matches(String plain, String hashed) {
        if (plain == null || hashed == null) {
            return false;
        }
        return BCrypt.checkpw(plain, hashed);
    }
}
