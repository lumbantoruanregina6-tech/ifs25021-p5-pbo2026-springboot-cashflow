package org.delcom._1s25021springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.delcom._1s25021springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom._1s25021springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom._1s25021springbootcashflow.adapter.storage.FileStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class AdapterGapTest {
    @Test
    void fileStorageGagalMembuatFolderUpload(@TempDir Path dir) throws Exception {
        Path berkas = Files.createFile(dir.resolve("bukan-folder"));
        String lokasiTidakValid = berkas.resolve("upload").toString();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> new FileStorage(lokasiTidakValid));
        assertTrue(e.getMessage().contains("folder upload"));
        assertNotNull(e.getCause());
    }

    @Test
    void jdbcUserRepositoryHapusIdTidakAda() {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.initialize(ds);
        JdbcUserRepository users = new JdbcUserRepository(ds);
        assertFalse(users.deleteById(999));
    }
}
