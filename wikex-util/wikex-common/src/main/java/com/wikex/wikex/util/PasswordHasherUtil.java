package com.wikex.wikex.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasherUtil {
    private PasswordHasherUtil() {
        throw new AssertionError("PasswordHasherUtil cannot be instantiated");
    }

    public enum PasswordHasherCompatibilityMode {
        IdentityV2,
        IdentityV3
    }

    public enum PasswordVerificationResult {
        Failed,
        Success,
        SuccessRehashNeeded
    }

    public enum KeyDerivationPrf {
        HMACSHA1,
        HMACSHA256,
        HMACSHA512
    }

    public static class PasswordHasherOptions {
        private PasswordHasherCompatibilityMode compatibilityMode = PasswordHasherCompatibilityMode.IdentityV3;
        private int iterationCount = 100000; // Match provided hash
        private SecureRandom rng = new SecureRandom();

        public PasswordHasherCompatibilityMode getCompatibilityMode() {
            return compatibilityMode;
        }

        public void setCompatibilityMode(PasswordHasherCompatibilityMode mode) {
            this.compatibilityMode = mode;
        }

        public int getIterationCount() {
            return iterationCount;
        }

        public void setIterationCount(int count) {
            this.iterationCount = count;
        }

        public SecureRandom getRng() {
            return rng;
        }

        public void setRng(SecureRandom rng) {
            this.rng = rng;
        }
    }

    private static final PasswordHasherOptions DEFAULT_OPTIONS = new PasswordHasherOptions();

    public static String hashPassword(String password, PasswordHasherOptions options) {
        if (password == null)
            throw new IllegalArgumentException("Password cannot be null");
        if (options == null)
            options = DEFAULT_OPTIONS;
        validateOptions(options);
        byte[] hashedBytes =
                // (options.getCompatibilityMode() ==
                // PasswordHasherCompatibilityMode.IdentityV2)
                // ? hashPasswordV2(password, options.getRng())
                // :
                hashPasswordV3(password, options.getRng(), options.getIterationCount());
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    public static String hashPassword(String password) {
        return hashPassword(password, DEFAULT_OPTIONS);
    }

    private static void validateOptions(PasswordHasherOptions options) {
        if (options.getCompatibilityMode() == PasswordHasherCompatibilityMode.IdentityV3
                && options.getIterationCount() < 1) {
            throw new IllegalArgumentException("Invalid password hasher iteration count");
        }
        if (options.getCompatibilityMode() == null) {
            throw new IllegalArgumentException("Invalid password hasher compatibility mode");
        }
    }

    private static byte[] hashPasswordV2(String password, SecureRandom rng) {
        final String pbkdf2Prf = "PBKDF2WithHmacSHA1";
        final int pbkdf2IterCount = 1000;
        final int pbkdf2SubkeyLength = 256 / 8;
        final int saltSize = 128 / 8;

        byte[] salt = new byte[saltSize];
        rng.nextBytes(salt);
        byte[] subkey = pbkdf2(password, salt, pbkdf2Prf, pbkdf2IterCount, pbkdf2SubkeyLength);

        byte[] outputBytes = new byte[1 + saltSize + pbkdf2SubkeyLength];
        outputBytes[0] = 0x00;
        System.arraycopy(salt, 0, outputBytes, 1, saltSize);
        System.arraycopy(subkey, 0, outputBytes, 1 + saltSize, pbkdf2SubkeyLength);
        return outputBytes;
    }

    private static byte[] hashPasswordV3(String password, SecureRandom rng, int iterCount) {
        return hashPasswordV3(password, rng, KeyDerivationPrf.HMACSHA256, iterCount, 128 / 8, 256 / 8);
    }

    private static byte[] hashPasswordV3(String password, SecureRandom rng, KeyDerivationPrf prf, int iterCount,
            int saltSize, int numBytesRequested) {
        final String pbkdf2Prf = prf == KeyDerivationPrf.HMACSHA512 ? "PBKDF2WithHmacSHA512"
                : prf == KeyDerivationPrf.HMACSHA256 ? "PBKDF2WithHmacSHA256" : "PBKDF2WithHmacSHA1";

        byte[] salt = new byte[saltSize];
        rng.nextBytes(salt);
        byte[] subkey = pbkdf2(password, salt, pbkdf2Prf, iterCount, numBytesRequested);

        byte[] outputBytes = new byte[13 + salt.length + subkey.length];
        outputBytes[0] = 0x01;
        writeNetworkByteOrder(outputBytes, 1, prf.ordinal());
        writeNetworkByteOrder(outputBytes, 5, iterCount);
        writeNetworkByteOrder(outputBytes, 9, saltSize);
        System.arraycopy(salt, 0, outputBytes, 13, salt.length);
        System.arraycopy(subkey, 0, outputBytes, 13 + saltSize, subkey.length);
        return outputBytes;
    }

    private static byte[] pbkdf2(String password, byte[] salt, String algorithm, int iterations, int numBytes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, numBytes * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("Error during PBKDF2 hashing", e);
        }
    }

    public static PasswordVerificationResult verifyHashedPassword(String hashedPassword, String providedPassword,
            PasswordHasherOptions options) {
        if (hashedPassword == null)
            throw new IllegalArgumentException("Hashed password cannot be null");
        if (providedPassword == null)
            throw new IllegalArgumentException("Provided password cannot be null");
        if (options == null)
            options = DEFAULT_OPTIONS;
        validateOptions(options);

        byte[] decodedHashedPassword;
        try {
            decodedHashedPassword = Base64.getDecoder().decode(hashedPassword);
        } catch (IllegalArgumentException e) {
            return PasswordVerificationResult.Failed;
        }

        if (decodedHashedPassword.length == 0) {
            return PasswordVerificationResult.Failed;
        }

        switch (decodedHashedPassword[0]) {
            case 0x00:
                if (verifyHashedPasswordV2(decodedHashedPassword, providedPassword)) {
                    return (options.getCompatibilityMode() == PasswordHasherCompatibilityMode.IdentityV3)
                            ? PasswordVerificationResult.SuccessRehashNeeded
                            : PasswordVerificationResult.Success;
                }
                return PasswordVerificationResult.Failed;
            case 0x01:
                int[] embeddedIterCount = new int[1];
                if (verifyHashedPasswordV3(decodedHashedPassword, providedPassword, embeddedIterCount)) {
                    return (embeddedIterCount[0] < options.getIterationCount())
                            ? PasswordVerificationResult.SuccessRehashNeeded
                            : PasswordVerificationResult.Success;
                }
                return PasswordVerificationResult.Failed;
            default:
                return PasswordVerificationResult.Failed;
        }
    }

    public static PasswordVerificationResult verifyHashedPassword(String hashedPassword, String providedPassword) {
        return verifyHashedPassword(hashedPassword, providedPassword, DEFAULT_OPTIONS);
    }

    private static boolean verifyHashedPasswordV2(byte[] hashedPassword, String password) {
        final String pbkdf2Prf = "PBKDF2WithHmacSHA1";
        final int pbkdf2IterCount = 1000;
        final int pbkdf2SubkeyLength = 256 / 8;
        final int saltSize = 128 / 8;

        if (hashedPassword.length != 1 + saltSize + pbkdf2SubkeyLength) {
            return false;
        }

        byte[] salt = new byte[saltSize];
        System.arraycopy(hashedPassword, 1, salt, 0, salt.length);
        byte[] expectedSubkey = new byte[pbkdf2SubkeyLength];
        System.arraycopy(hashedPassword, 1 + salt.length, expectedSubkey, 0, expectedSubkey.length);

        byte[] actualSubkey = pbkdf2(password, salt, pbkdf2Prf, pbkdf2IterCount, pbkdf2SubkeyLength);
        return byteArraysEqual(actualSubkey, expectedSubkey);
    }

    private static boolean verifyHashedPasswordV3(byte[] hashedPassword, String password, int[] iterCount) {
        try {
            int prfOrdinal = (int) readNetworkByteOrder(hashedPassword, 1);
            if (prfOrdinal < 0 || prfOrdinal >= KeyDerivationPrf.values().length) {
                return false;
            }
            KeyDerivationPrf prf = KeyDerivationPrf.values()[prfOrdinal];
            iterCount[0] = (int) readNetworkByteOrder(hashedPassword, 5);
            int saltLength = (int) readNetworkByteOrder(hashedPassword, 9);

            if (saltLength < 128 / 8) {
                return false;
            }

            byte[] salt = new byte[saltLength];
            System.arraycopy(hashedPassword, 13, salt, 0, salt.length);

            int subkeyLength = hashedPassword.length - 13 - salt.length;
            if (subkeyLength < 128 / 8) {
                return false;
            }

            byte[] expectedSubkey = new byte[subkeyLength];
            System.arraycopy(hashedPassword, 13 + salt.length, expectedSubkey, 0, expectedSubkey.length);

            String algorithm = prf == KeyDerivationPrf.HMACSHA512 ? "PBKDF2WithHmacSHA512"
                    : prf == KeyDerivationPrf.HMACSHA256 ? "PBKDF2WithHmacSHA256" : "PBKDF2WithHmacSHA1";
            byte[] actualSubkey = pbkdf2(password, salt, algorithm, iterCount[0], subkeyLength);
            return byteArraysEqual(actualSubkey, expectedSubkey);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean byteArraysEqual(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    private static long readNetworkByteOrder(byte[] buffer, int offset) {
        return ((long) (buffer[offset] & 0xFF) << 24)
                | ((long) (buffer[offset + 1] & 0xFF) << 16)
                | ((long) (buffer[offset + 2] & 0xFF) << 8)
                | (buffer[offset + 3] & 0xFF);
    }

    private static void writeNetworkByteOrder(byte[] buffer, int offset, int value) {
        buffer[offset] = (byte) (value >> 24);
        buffer[offset + 1] = (byte) (value >> 16);
        buffer[offset + 2] = (byte) (value >> 8);
        buffer[offset + 3] = (byte) value;
    }
}