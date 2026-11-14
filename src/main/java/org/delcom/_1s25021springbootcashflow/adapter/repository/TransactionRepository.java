package org.delcom._1s25021springbootcashflow.adapter.repository;

import org.delcom._1s25021springbootcashflow.domain.entity.Transaction;
import org.delcom._1s25021springbootcashflow.domain.entity.TransactionType;
import org.delcom._1s25021springbootcashflow.domain.repository.ITransactionRepository;
import java.time.LocalDateTime;
import java.util.List;

public class TransactionRepository extends InMemoryRepository<Transaction> implements ITransactionRepository {
    @Override
    protected int getId(Transaction entity) {
        return entity.getId();
    }

    @Override
    protected void setId(Transaction entity, int id) {
        entity.setId(id);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        return save(new Transaction(0, userId, description, amount, type, proofPath, LocalDateTime.now()));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(t -> t.getUserId() == userId);
    }
}
