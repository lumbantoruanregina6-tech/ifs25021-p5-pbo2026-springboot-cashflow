package org.delcom._1s25021springbootcashflow.domain.repository;

import org.delcom._1s25021springbootcashflow.domain.entity.Transaction;
import org.delcom._1s25021springbootcashflow.domain.entity.TransactionType;
import java.util.List;

public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    Transaction save(int userId, String description, long amount, TransactionType type, String proofPath);

    List<Transaction> findByUserId(int userId);
}
