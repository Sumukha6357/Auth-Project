package com.example.idp.security.oauth;

public final class RefreshTokenContextHolder {
    private static final ThreadLocal<String> CURRENT_PARENT_HASH = new ThreadLocal<>();

    private RefreshTokenContextHolder() {
    }

    public static void setParentHash(String hash) {
        CURRENT_PARENT_HASH.set(hash);
    }

    public static String getParentHash() {
        return CURRENT_PARENT_HASH.get();
    }

    public static void clear() {
        CURRENT_PARENT_HASH.remove();
    }
}
