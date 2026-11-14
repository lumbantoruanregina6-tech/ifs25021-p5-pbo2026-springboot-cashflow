package org.delcom._1s25021springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom._1s25021springbootcashflow.domain.PasswordHasher;
import org.delcom._1s25021springbootcashflow.domain.entity.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DomainTest {
    @Test
    void passwordHasherSha256() {
        assertEquals("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", PasswordHasher.hash("password"));
        assertTrue(PasswordHasher.verify("password", PasswordHasher.hash("password")));
        assertFalse(PasswordHasher.verify("lain", PasswordHasher.hash("password")));
    }

    @Test
    void signedAmountAndSortOptions() {
        LocalDateTime now = LocalDateTime.now();
        Transaction in = new Transaction(1, 1, "Gaji", 100, TransactionType.INCOME, null, now);
        Transaction out = new Transaction(2, 1, "Makan", 300, TransactionType.EXPENSE, null, now.plusMinutes(1));
        assertEquals(100, in.signedAmount());
        assertEquals(-300, out.signedAmount());

        for (SortOption option : SortOption.values()) {
            List<Transaction> list = new ArrayList<>(List.of(in, out));
            list.sort(option.getComparator());
            assertEquals(2, list.size());
            assertNotNull(option.getLabel());
        }
        List<Transaction> byAmountDesc = new ArrayList<>(List.of(in, out));
        byAmountDesc.sort(SortOption.AMOUNT_DESC.getComparator());
        assertEquals(2, byAmountDesc.get(0).getId());
        List<Transaction> incomeFirst = new ArrayList<>(List.of(out, in));
        incomeFirst.sort(SortOption.INCOME_FIRST.getComparator());
        assertEquals(TransactionType.INCOME, incomeFirst.get(0).getType());
    }

    @Test
    void userRole() {
        User admin = new User(1, "a", "h", "A", Role.ADMIN, null);
        User user = new User(2, "b", "h", "B", Role.USER, null);
        assertTrue(admin.isAdmin());
        assertFalse(user.isAdmin());
    }
}
