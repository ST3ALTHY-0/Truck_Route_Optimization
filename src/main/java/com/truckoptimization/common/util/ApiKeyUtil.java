package com.truckoptimization.common.util;

public final class ApiKeyUtil {

    private ApiKeyUtil() {
        // Utility class
    }

    //prob not needed with the new .properties file instead of .env
    public static String requireApiKey(String rawKey, String envVarName) {
        String key = rawKey == null ? "" : rawKey.trim();

        if ((key.startsWith("\"") && key.endsWith("\"")) || (key.startsWith("'") && key.endsWith("'"))) {
            key = key.substring(1, key.length() - 1).trim();
        }

        if (key.isEmpty()) {
            throw new IllegalStateException(
                    "Missing " + envVarName + ". Set it in application.properties or as an environment variable.");
        }

        return key;
    }
}
