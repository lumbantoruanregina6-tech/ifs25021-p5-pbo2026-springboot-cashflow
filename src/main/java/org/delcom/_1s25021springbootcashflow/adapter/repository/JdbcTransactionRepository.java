package org.delcom._1s25021springbootcashflow.adapter.repository;

import org.delcom._1s25021springbootcashflow.domain.entity.Transaction;
import org.delcom._1s25021springbootcashflow.domain.entity.TransactionType;
import org.delcom._1s25021springbootcashflow.domain.repository.ITransactionRepository;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class JdbcTransactionRepository implements ITransactionRepository {
    private static final RowMapper<Transaction> MAPPER = JdbcTransactionRepository::map;

    private final JdbcTemplate jdbc;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    private static Transaction map(ResultSet rs, int rowNum) throws SQLException {
        return new Transaction(rs.getInt("id"), rs.getInt("user_id"), rs.getString("description"),
                rs.getLong("amount"), TransactionType.valueOf(rs.getString("type")), rs.getString("proof_path"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        return save(new Transaction(0, userId, description, amount, type, proofPath, LocalDateTime.now()));
    }

    @Override
    public Transaction save(Transaction t) {
        if (t.getId() == 0) {
            KeyHolder keys = new GeneratedKeyHolder();
            jdbc.update(con -> {
                PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                        new String[]{"id"});
                ps.setInt(1, t.getUserId());
                ps.setString(2, t.getDescription());
                ps.setLong(3, t.getAmount());
                ps.setString(4, t.getType().name());
                ps.setString(5, t.getProofPath());
                ps.setTimestamp(6, Timestamp.valueOf(t.getCreatedAt()));
                return ps;
            }, keys);
            t.setId(keys.getKey().intValue());
        } else {
            jdbc.update("UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?",
                    t.getDescription(), t.getAmount(), t.getType().name(), t.getProofPath(), t.getId());
        }
        return t;
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        return jdbc.query("SELECT * FROM transactions WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        return jdbc.query("SELECT * FROM transactions ORDER BY id", MAPPER);
    }

    @Override
    public List<Transaction> findBy(Predicate<Transaction> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return jdbc.query("SELECT * FROM transactions WHERE user_id = ? ORDER BY id", MAPPER, userId);
    }

    @Override
    public boolean deleteById(Integer id) {
        return jdbc.update("DELETE FROM transactions WHERE id = ?", id) > 0;
    }
}
