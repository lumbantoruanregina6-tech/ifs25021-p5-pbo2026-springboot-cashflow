package org.delcom._1s25021springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom._1s25021springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom._1s25021springbootcashflow.adapter.repository.UserRepository;
import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.exception.ValidationException;
import org.delcom._1s25021springbootcashflow.usecase.AuthUseCase;
import org.delcom._1s25021springbootcashflow.usecase.FinanceUseCase;
import org.delcom._1s25021springbootcashflow.usecase.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Melengkapi cabang (if/else) yang belum tersentuh oleh UseCaseTest. */
class UseCaseBranchTest {
    private AuthUseCase auth;
    private UserUseCase userUseCase;
    private FinanceUseCase finance;
    private User budi;

    @BeforeEach
    void setUp() throws Exception {
        UserRepository users = new UserRepository();
        auth = new AuthUseCase(users);
        userUseCase = new UserUseCase(users);
        finance = new FinanceUseCase(new TransactionRepository());
        budi = auth.register("budi", "rahasia", "Budi");
    }

    @Test
    void loginMenolakUsernameAtauPasswordKosongDanNull() {
        assertThrows(ValidationException.class, () -> auth.login("budi", ""));
        assertThrows(ValidationException.class, () -> auth.login("budi", null));
        assertThrows(ValidationException.class, () -> auth.login(null, "rahasia"));
    }

    @Test
    void updateProfileDenganNamaNullDitolak() {
        assertThrows(ValidationException.class, () -> userUseCase.updateProfile(budi.getId(), null, "abcd"));
    }

    @Test
    void updateProfileTanpaPasswordBaruTidakMengubahHash() throws Exception {
        String hashLama = budi.getPasswordHash();
        User hasil = userUseCase.updateProfile(budi.getId(), "Budi Santoso", null);
        assertEquals("Budi Santoso", hasil.getFullName());
        assertEquals(hashLama, hasil.getPasswordHash());
    }

    @Test
    void keteranganNullDitolak() {
        assertThrows(ValidationException.class, () -> finance.addIncome(budi.getId(), null, "10", null));
        assertThrows(ValidationException.class, () -> finance.addExpense(budi.getId(), null, "10", null));
    }
}
