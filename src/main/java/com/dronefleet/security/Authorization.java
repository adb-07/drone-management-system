package com.dronefleet.security;

public final class Authorization {
    private Authorization() {}
    public static void requireAdmin(Session session) {
        if (session == null || !session.admin()) {
            throw new SecurityException("Administrator privileges are required for this operation.");
        }
    }
}
