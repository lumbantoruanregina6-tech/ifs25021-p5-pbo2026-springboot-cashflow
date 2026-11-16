package org.delcom._1s25021springbootcashflow;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

class AppMainTest {
    /**
     * Menjalankan App.main tanpa benar-benar menyalakan server: SpringApplication.run
     * di-mock supaya tidak membuka port dan tidak memicu Vaadin dev mode.
     * System property yang ditambahkan DotEnv (jika ada file .env) dibersihkan lagi.
     */
    @Test
    void mainMemuatDotEnvLaluMenjalankanSpringApplication() throws Exception {
        String[] args = {"--tes=1"};
        Set<String> sebelum = System.getProperties().stringPropertyNames();
        try (MockedStatic<SpringApplication> spring = Mockito.mockStatic(SpringApplication.class)) {
            App.main(args);
            spring.verify(() -> SpringApplication.run(App.class, args));
        } finally {
            for (String key : System.getProperties().stringPropertyNames()) {
                if (!sebelum.contains(key)) {
                    System.clearProperty(key);
                }
            }
        }
    }
}
