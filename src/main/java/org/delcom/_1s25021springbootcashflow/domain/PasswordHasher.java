package org.delcom._1s25021springbootcashflow.domain;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordHasher {
    private static final String ALGORITHM = "SHA-256";

    private PasswordHasher() {
    }

    /** SHA-256 dalam format hex. */
    public static String hash(String raw) {
        return digest(ALGORITHM, raw);
    }

    public static boolean verify(String raw, String hash) {
        return hash(raw).equals(hash);
    }

    /** Dipisah dari hash() agar jalur gagal (algoritma tidak tersedia) bisa dites. */
    static String digest(String algorithm, String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(algorithm + " tidak tersedia", e);
        }
    }
}
