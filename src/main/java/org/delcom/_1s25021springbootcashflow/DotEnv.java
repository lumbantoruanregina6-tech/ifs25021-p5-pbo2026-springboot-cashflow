package org.delcom._1s25021springbootcashflow;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Memuat file .env (jika ada) ke System properties sebelum Spring Boot start. */
public final class DotEnv {
    private DotEnv() {}

    public static void load() throws Exception {
        load(Path.of(".env"));
    }

    static void load(Path env) throws Exception {
        if (!Files.exists(env)) return;
        List<String> lines = Files.readAllLines(env);
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
            int i = line.indexOf('=');
            String key = line.substring(0, i).trim();
            String value = line.substring(i + 1).trim();
            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, value);
            }
        }
    }
}
