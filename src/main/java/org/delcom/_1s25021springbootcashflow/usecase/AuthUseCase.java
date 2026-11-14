package org.delcom._1s25021springbootcashflow.usecase;

import org.delcom._1s25021springbootcashflow.domain.PasswordHasher;
import org.delcom._1s25021springbootcashflow.domain.entity.Role;
import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.exception.DomainException;
import org.delcom._1s25021springbootcashflow.domain.exception.ValidationException;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;

public class AuthUseCase {
    private final IUserRepository users;

    public AuthUseCase(IUserRepository users) {
        this.users = users;
    }

    public User register(String username, String password, String fullName) throws DomainException {
        if (isBlank(username)) throw new ValidationException("Username tidak boleh kosong");
        if (isBlank(fullName)) throw new ValidationException("Nama lengkap tidak boleh kosong");
        if (password == null || password.length() < 4) {
            throw new ValidationException("Password minimal 4 karakter");
        }
        if (users.findByUsername(username.trim()).isPresent()) {
            throw new ValidationException("Username sudah dipakai");
        }
        return users.save(new User(0, username.trim(), PasswordHasher.hash(password), fullName.trim(), Role.USER, null));
    }

    public User login(String username, String password) throws DomainException {
        if (isBlank(username) || isBlank(password)) {
            throw new ValidationException("Username dan password wajib diisi");
        }
        User user = users.findByUsername(username.trim())
                .orElseThrow(() -> new ValidationException("Username atau password salah"));
        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new ValidationException("Username atau password salah");
        }
        return user;
    }

    /** Membuat admin awal dari ROOT_USERNAME / ROOT_PASSWORD bila belum ada. */
    public void ensureRootAdmin(String username, String password) {
        if (users.findByUsername(username).isEmpty()) {
            users.save(new User(0, username, PasswordHasher.hash(password), "Administrator", Role.ADMIN, null));
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
