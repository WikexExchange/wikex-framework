package com.wikex.wikex.util;

public final class EmailUtil {

    private EmailUtil() {
    }

    public static String normalize(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        String withoutSpaces = trimmed.replaceAll("\\s+", "");
        return withoutSpaces.toLowerCase();
    }
}
