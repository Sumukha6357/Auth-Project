package com.example.idp.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class TokenUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private TokenUtils() {
    }

    public static String randomUrlSafeToken(int bytes) {
        byte[] buffer = new byte[bytes];
        RANDOM.nextBytes(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
    }
}
