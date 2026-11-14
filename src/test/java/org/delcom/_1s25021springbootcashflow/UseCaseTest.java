package org.delcom._1s25021springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom._1s25021springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom._1s25021springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom._1s25021springbootcashflow.adapter.repository.UserRepository;
import org.delcom._1s25021springbootcashflow.domain.entity.*;
import org.delcom._1s25021springbootcashflow.domain.exception.*;
import org.delcom._1s25021springbootcashflow.usecase.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UseCaseTest {
    private UserRepository users;
    private AuthUseCase auth;
    private UserUseCase userUseCase;
    private FinanceUseCase finance;
    private FinancePresenter presenter;
    private User budi;

    @BeforeEach
    void setUp() throws Exception {
        users = new UserRepository();
        auth = new AuthUseCase(users);
        userUseCase = new UserUseCase(users);
        finance = new FinanceUseCase(new TransactionRepository());
        presenter = new FinancePresenter();
        budi = auth.register("budi", "rahasia", "Budi");
    }

    @Test
    void authFlow() throws Exception {
        auth.ensureRootAdmin("admin", "admin");
        auth.ensureRootAdmin("admin", "admin");
        assertEquals(2, users.findAll().size());
        assertEquals(Role.ADMIN, auth.login("admin", "admin").getRole());
        assertThrows(ValidationException.class, () -> auth.login("admin", "salah"));
        assertThrows(ValidationException.class, () -> auth.login("tidakada", "x"));
        assertThrows(ValidationException.class, () -> auth.login("", ""));
        assertThrows(ValidationException.class, () -> auth.register("budi", "rahasia", "Budi"));
        assertThrows(ValidationException.class, () -> auth.register(" ", "rahasia", "X"));
        assertThrows(ValidationException.class, () -> auth.register("x", "rahasia", " "));
        assertThrows(ValidationException.class, () -> auth.register("x", "abc", "X"));
        assertThrows(ValidationException.class, () -> auth.register("x", null, "X"));
    }

    @Test
    void userUseCase() throws Exception {
        assertEquals(1, userUseCase.listUsers().size());
        assertEquals("Budi", userUseCase.getUser(budi.getId()).getFullName());
        assertThrows(ValidationException.class, () -> userUseCase.getUser(0));
        assertThrows(EntityNotFoundException.class, () -> userUseCase.getUser(99));

        userUseCase.updateProfile(budi.getId(), "Budi Baru", "");
        assertEquals("Budi Baru", users.findById(budi.getId()).orElseThrow().getFullName());
        String oldHash = budi.getPasswordHash();
        userUseCase.updateProfile(budi.getId(), "Budi Baru", "passwordbaru");
        assertNotEquals(oldHash, users.findById(budi.getId()).orElseThrow().getPasswordHash());
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(budi.getId(), " ", null));
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(budi.getId(), "X", "abc"));

        assertEquals("foto.png", userUseCase.updatePhoto(budi.getId(), "foto.png").getPhotoPath());
    }

    @Test
    void financeFlow() throws Exception {
        int id = budi.getId();
        finance.addIncome(id, "Gaji", "5000000", null);
        finance.addExpense(id, "Makan", "25000", "bukti.png");
        assertEquals(4975000, finance.getBalance(id));
        assertEquals(2, finance.getSummary(id).count());

        assertEquals(1, finance.list(id, "GAJ", SortOption.NEWEST).size());
        assertEquals(2, finance.list(id, null, null).size());
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(finance.getBalance(id)));

        Transaction updated = finance.update(id, 2, "Makan siang", "30000", null);
        assertEquals("bukti.png", updated.getProofPath());
        assertEquals(30000, updated.getAmount());
        finance.update(id, 2, "Makan siang", "30000", "baru.png");

        finance.delete(id, 2);
        assertEquals(5000000, finance.getBalance(id));
    }

    @Test
    void financeValidation() {
        int id = budi.getId();
        for (String bad : new String[]{null, "", "abc", "0", "-5"}) {
            assertThrows(ValidationException.class, () -> finance.addIncome(id, "x", bad, null));
        }
        assertThrows(ValidationException.class, () -> finance.addExpense(id, " ", "10", null));
        assertThrows(ValidationException.class, () -> finance.delete(id, 0));
        assertThrows(EntityNotFoundException.class, () -> finance.delete(id, 99));
    }

    @Test
    void cannotTouchOthersTransaction() throws Exception {
        finance.addIncome(budi.getId(), "Gaji", "100", null);
        assertThrows(ValidationException.class, () -> finance.delete(999, 1));
        assertThrows(ValidationException.class, () -> finance.update(999, 1, "x", "5", null));
    }

    @Test
    void presenterFormats() throws Exception {
        Transaction t = finance.addIncome(budi.getId(), "Gaji", "5000000", null);
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1000000));
        assertEquals("-Rp 1,000.00", presenter.formatMoney(-1000));
        assertEquals("[1] Gaji | Rp 5,000,000.00 | [Pemasukan]", presenter.formatRow(t));
        assertEquals("-", presenter.formatTime(null));
        assertFalse(presenter.formatTime(t.getCreatedAt()).isBlank());
        assertTrue(presenter.addSuccess(t).contains("Pemasukan"));
        assertTrue(presenter.updateSuccess(t).contains("#1"));
        assertTrue(presenter.deleteSuccess(1).contains("#1"));
        assertTrue(presenter.failure("x").startsWith("Gagal"));
        assertEquals("Saldo: Rp 5,000,000.00", presenter.formatSummary(finance.getSummary(budi.getId())));
    }
}
