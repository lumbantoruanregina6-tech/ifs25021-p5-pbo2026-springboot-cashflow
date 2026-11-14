package org.delcom._1s25021springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom._1s25021springbootcashflow.adapter.repository.*;
import org.delcom._1s25021springbootcashflow.adapter.storage.FileStorage;
import org.delcom._1s25021springbootcashflow.domain.entity.*;
import org.delcom._1s25021springbootcashflow.domain.exception.ValidationException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class AdapterTest {
    @Test
    void inMemoryRepositories() {
        UserRepository users = new UserRepository();
        User u = users.save(new User(0, "a", "h", "A", Role.USER, null));
        assertEquals(1, u.getId());
        assertTrue(users.findByUsername("a").isPresent());
        assertTrue(users.findByUsername("zzz").isEmpty());
        assertEquals(1, users.findBy(x -> x.getRole() == Role.USER).size());
        users.save(u);
        assertEquals(1, users.findAll().size());
        assertTrue(users.deleteById(1));
        assertFalse(users.deleteById(1));

        TransactionRepository tx = new TransactionRepository();
        tx.save(1, "Gaji", 10, TransactionType.INCOME, null);
        tx.save(2, "Makan", 5, TransactionType.EXPENSE, null);
        assertEquals(1, tx.findByUserId(1).size());
        assertTrue(tx.findById(2).isPresent());
    }

    @Test
    void fileStorage(@TempDir Path dir) throws Exception {
        FileStorage storage = new FileStorage(dir.resolve("up").toString());
        String name = storage.store("Foto.PNG", new byte[]{1, 2, 3});
        assertTrue(name.endsWith(".png"));
        assertTrue(Files.exists(storage.getRoot().resolve(name)));
        assertEquals("/uploads/" + name, FileStorage.url(name));
        assertThrows(ValidationException.class, () -> storage.store("virus.exe", new byte[]{1}));
        assertThrows(ValidationException.class, () -> storage.store("tanpaekstensi", new byte[]{1}));
        assertThrows(ValidationException.class, () -> storage.store(null, new byte[]{1}));
        storage.delete(name);
        storage.delete(null);
        storage.delete(" ");
        assertFalse(Files.exists(storage.getRoot().resolve(name)));
    }

    @Test
    void jdbcRepositoriesOnH2() {
        DriverManagerDataSource ds = new DriverManagerDataSource(
                "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        SchemaInitializer.initialize(ds);
        SchemaInitializer.initialize(ds); // idempoten

        JdbcUserRepository users = new JdbcUserRepository(ds);
        User u = users.save(new User(0, "budi", "hash", "Budi", Role.USER, null));
        assertTrue(u.getId() > 0);
        u.setFullName("Budi Baru");
        users.save(u);
        assertEquals("Budi Baru", users.findById(u.getId()).orElseThrow().getFullName());
        assertTrue(users.findByUsername("budi").isPresent());
        assertTrue(users.findById(999).isEmpty());
        assertEquals(1, users.findAll().size());
        assertEquals(1, users.findBy(x -> x.getUsername().equals("budi")).size());

        JdbcTransactionRepository tx = new JdbcTransactionRepository(ds);
        Transaction t = tx.save(u.getId(), "Gaji", 5000000, TransactionType.INCOME, null);
        assertTrue(t.getId() > 0);
        t.setDescription("Gaji bulanan");
        t.setProofPath("b.png");
        tx.save(t);
        Transaction loaded = tx.findById(t.getId()).orElseThrow();
        assertEquals("Gaji bulanan", loaded.getDescription());
        assertEquals("b.png", loaded.getProofPath());
        assertEquals(1, tx.findByUserId(u.getId()).size());
        assertEquals(1, tx.findAll().size());
        assertEquals(1, tx.findBy(x -> x.getAmount() == 5000000).size());
        assertTrue(tx.deleteById(t.getId()));
        assertFalse(tx.deleteById(t.getId()));
        assertTrue(users.deleteById(u.getId()));
    }
}
