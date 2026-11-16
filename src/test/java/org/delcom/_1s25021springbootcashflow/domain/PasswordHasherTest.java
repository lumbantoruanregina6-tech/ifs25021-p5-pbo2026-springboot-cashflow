package org.delcom._1s25021springbootcashflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {
    @Test
    void digestMenghasilkanHexSha256() {
        assertEquals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
                PasswordHasher.digest("SHA-256", "password"));
    }

    @Test
    void algoritmaTidakTersediaMelemparIllegalState() {
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> PasswordHasher.digest("ALGORITMA-PALSU", "x"));
        assertTrue(e.getMessage().contains("ALGORITMA-PALSU"));
        assertNotNull(e.getCause());
    }
}
