package org.delcom._1s25021springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DotEnvTest {
    private static final String NEW_KEY = "DOTENV_TEST_KEY_BARU";
    private static final String EXISTING_KEY = "DOTENV_TEST_KEY_SUDAH_ADA";

    @AfterEach
    void bersihkan() {
        System.clearProperty(NEW_KEY);
        System.clearProperty(EXISTING_KEY);
        System.clearProperty("PATH");
    }

    @Test
    void fileTidakAdaTidakMelakukanApaApa(@TempDir Path dir) throws Exception {
        DotEnv.load(dir.resolve("tidak-ada.env"));
        assertNull(System.getProperty(NEW_KEY));
    }

    @Test
    void membacaBarisDanMelewatiYangTidakValid(@TempDir Path dir) throws Exception {
        Path env = dir.resolve(".env");
        Files.writeString(env, String.join("\n",
                "# komentar",
                "",
                "    ",
                "BARIS_TANPA_TANDA_SAMA",
                NEW_KEY + " = nilai=dengan=sama  ",
                EXISTING_KEY + "=dari-file",
                "PATH=tidak-boleh-menimpa-env"));
        System.setProperty(EXISTING_KEY, "asli");

        DotEnv.load(env);

        assertEquals("nilai=dengan=sama", System.getProperty(NEW_KEY));
        assertEquals("asli", System.getProperty(EXISTING_KEY));
        assertNull(System.getProperty("PATH"));
        assertNull(System.getProperty("BARIS_TANPA_TANDA_SAMA"));
    }
}
