package org.delcom._1s25021springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.delcom._1s25021springbootcashflow.domain.entity.Role;
import org.delcom._1s25021springbootcashflow.domain.entity.Transaction;
import org.delcom._1s25021springbootcashflow.domain.entity.TransactionType;
import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.Test;

class EntityAccessorTest {
    @Test
    void transactionGetterSetter() {
        LocalDateTime t1 = LocalDateTime.of(2026, 1, 1, 8, 0);
        LocalDateTime t2 = LocalDateTime.of(2026, 2, 2, 9, 30);
        Transaction t = new Transaction(1, 10, "Gaji", 100, TransactionType.INCOME, null, t1);
        assertEquals(1, t.getId());
        assertEquals(10, t.getUserId());
        assertEquals("Gaji", t.getDescription());
        assertEquals(100, t.getAmount());
        assertEquals(TransactionType.INCOME, t.getType());
        assertNull(t.getProofPath());
        assertEquals(t1, t.getCreatedAt());

        t.setId(2);
        t.setUserId(20);
        t.setDescription("Makan");
        t.setAmount(250);
        t.setType(TransactionType.EXPENSE);
        t.setProofPath("bukti.png");
        t.setCreatedAt(t2);

        assertEquals(2, t.getId());
        assertEquals(20, t.getUserId());
        assertEquals("Makan", t.getDescription());
        assertEquals(250, t.getAmount());
        assertEquals(TransactionType.EXPENSE, t.getType());
        assertEquals("bukti.png", t.getProofPath());
        assertEquals(t2, t.getCreatedAt());
        assertEquals(-250, t.signedAmount());
    }

    @Test
    void userGetterSetter() {
        User u = new User(1, "budi", "hash", "Budi", Role.USER, null);
        assertEquals(1, u.getId());
        assertEquals("budi", u.getUsername());
        assertEquals("hash", u.getPasswordHash());
        assertEquals("Budi", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertNull(u.getPhotoPath());
        assertFalse(u.isAdmin());

        u.setId(2);
        u.setUsername("andi");
        u.setPasswordHash("hash2");
        u.setFullName("Andi");
        u.setRole(Role.ADMIN);
        u.setPhotoPath("foto.png");

        assertEquals(2, u.getId());
        assertEquals("andi", u.getUsername());
        assertEquals("hash2", u.getPasswordHash());
        assertEquals("Andi", u.getFullName());
        assertEquals(Role.ADMIN, u.getRole());
        assertEquals("foto.png", u.getPhotoPath());
        assertTrue(u.isAdmin());
    }
}
