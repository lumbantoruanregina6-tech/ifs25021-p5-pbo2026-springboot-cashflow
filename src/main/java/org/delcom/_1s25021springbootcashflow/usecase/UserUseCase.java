package org.delcom._1s25021springbootcashflow.usecase;

import org.delcom._1s25021springbootcashflow.domain.PasswordHasher;
import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.exception.DomainException;
import org.delcom._1s25021springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom._1s25021springbootcashflow.domain.exception.ValidationException;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import java.util.List;

public class UserUseCase {
    private final IUserRepository users;

    public UserUseCase(IUserRepository users) {
        this.users = users;
    }

    public List<User> listUsers() {
        return users.findAll();
    }

    public User getUser(int id) throws DomainException {
        if (id <= 0) throw new ValidationException("ID pengguna tidak valid");
        return users.findById(id).orElseThrow(() -> new EntityNotFoundException("Pengguna tidak ditemukan"));
    }

    /** newPassword boleh kosong = password tidak diubah. */
    public User updateProfile(int id, String fullName, String newPassword) throws DomainException {
        User user = getUser(id);
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Nama lengkap tidak boleh kosong");
        }
        user.setFullName(fullName.trim());
        if (newPassword != null && !newPassword.isEmpty()) {
            if (newPassword.length() < 4) throw new ValidationException("Password minimal 4 karakter");
            user.setPasswordHash(PasswordHasher.hash(newPassword));
        }
        return users.save(user);
    }

    public User updatePhoto(int id, String photoPath) throws DomainException {
        User user = getUser(id);
        user.setPhotoPath(photoPath);
        return users.save(user);
    }
}
