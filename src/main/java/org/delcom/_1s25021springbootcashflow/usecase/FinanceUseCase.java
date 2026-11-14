package org.delcom._1s25021springbootcashflow.usecase;

import org.delcom._1s25021springbootcashflow.domain.entity.SortOption;
import org.delcom._1s25021springbootcashflow.domain.entity.Transaction;
import org.delcom._1s25021springbootcashflow.domain.entity.TransactionType;
import org.delcom._1s25021springbootcashflow.domain.exception.DomainException;
import org.delcom._1s25021springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom._1s25021springbootcashflow.domain.exception.ValidationException;
import org.delcom._1s25021springbootcashflow.domain.repository.ITransactionRepository;
import java.util.List;

public class FinanceUseCase {

    /** Ringkasan keuangan satu pengguna. */
    public record Summary(int count, long income, long expense, long balance) {
    }

    private final ITransactionRepository transactions;

    public FinanceUseCase(ITransactionRepository transactions) {
        this.transactions = transactions;
    }

    public Transaction addIncome(int userId, String description, String amountText, String proofPath)
            throws DomainException {
        return add(userId, description, amountText, TransactionType.INCOME, proofPath);
    }

    public Transaction addExpense(int userId, String description, String amountText, String proofPath)
            throws DomainException {
        return add(userId, description, amountText, TransactionType.EXPENSE, proofPath);
    }

    private Transaction add(int userId, String description, String amountText, TransactionType type,
                            String proofPath) throws DomainException {
        String desc = validateDescription(description);
        long amount = parseAmount(amountText);
        return transactions.save(userId, desc, amount, type, proofPath);
    }

    /** Daftar transaksi milik userId, difilter keterangan (case-insensitive) dan diurutkan. */
    public List<Transaction> list(int userId, String keyword, SortOption sort) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        SortOption option = sort == null ? SortOption.NEWEST : sort;
        return transactions.findByUserId(userId).stream()
                .filter(t -> key.isEmpty() || t.getDescription().toLowerCase().contains(key))
                .sorted(option.getComparator())
                .toList();
    }

    public Transaction update(int userId, int id, String description, String amountText, String proofPath)
            throws DomainException {
        Transaction t = findOwned(userId, id);
        t.setDescription(validateDescription(description));
        t.setAmount(parseAmount(amountText));
        if (proofPath != null) {
            t.setProofPath(proofPath);
        }
        return transactions.save(t);
    }

    public void delete(int userId, int id) throws DomainException {
        findOwned(userId, id);
        transactions.deleteById(id);
    }

    public Summary getSummary(int userId) {
        List<Transaction> all = transactions.findByUserId(userId);
        long income = all.stream().filter(t -> t.getType() == TransactionType.INCOME)
                .mapToLong(Transaction::getAmount).sum();
        long expense = all.stream().filter(t -> t.getType() == TransactionType.EXPENSE)
                .mapToLong(Transaction::getAmount).sum();
        return new Summary(all.size(), income, expense, income - expense);
    }

    public long getBalance(int userId) {
        return getSummary(userId).balance();
    }

    private Transaction findOwned(int userId, int id) throws DomainException {
        if (id <= 0) throw new ValidationException("ID transaksi tidak valid");
        Transaction t = transactions.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaksi tidak ditemukan"));
        if (t.getUserId() != userId) {
            throw new ValidationException("Transaksi bukan milik Anda");
        }
        return t;
    }

    private static String validateDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
        return description.trim();
    }

    private static long parseAmount(String text) throws ValidationException {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        long value;
        try {
            value = Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        if (value <= 0) {
            throw new ValidationException("Jumlah harus lebih dari 0");
        }
        return value;
    }
}
