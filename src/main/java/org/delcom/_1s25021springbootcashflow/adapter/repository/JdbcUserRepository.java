package org.delcom._1s25021springbootcashflow.adapter.repository;

import org.delcom._1s25021springbootcashflow.domain.entity.Role;
import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class JdbcUserRepository implements IUserRepository {
    private static final RowMapper<User> MAPPER = JdbcUserRepository::map;

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    private static User map(ResultSet rs, int rowNum) throws SQLException {
        return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password_hash"),
                rs.getString("full_name"), Role.valueOf(rs.getString("role")), rs.getString("photo_path"));
    }

    @Override
    public User save(User user) {
        if (user.getId() == 0) {
            KeyHolder keys = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO users (username, password_hash, full_name, role, photo_path) VALUES (?, ?, ?, ?, ?)",
                        new String[]{"id"});
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getPasswordHash());
                ps.setString(3, user.getFullName());
                ps.setString(4, user.getRole().name());
                ps.setString(5, user.getPhotoPath());
                return ps;
            }, keys);
            user.setId(keys.getKey().intValue());
        } else {
            jdbc.update("UPDATE users SET username = ?, password_hash = ?, full_name = ?, role = ?, photo_path = ? WHERE id = ?",
                    user.getUsername(), user.getPasswordHash(), user.getFullName(), user.getRole().name(),
                    user.getPhotoPath(), user.getId());
        }
        return user;
    }

    @Override
    public Optional<User> findById(Integer id) {
        return jdbc.query("SELECT * FROM users WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jdbc.query("SELECT * FROM users WHERE username = ?", MAPPER, username).stream().findFirst();
    }

    @Override
    public List<User> findAll() {
        return jdbc.query("SELECT * FROM users ORDER BY id", MAPPER);
    }

    @Override
    public List<User> findBy(Predicate<User> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    @Override
    public boolean deleteById(Integer id) {
        return jdbc.update("DELETE FROM users WHERE id = ?", id) > 0;
    }
}
