package org.delcom._1s25021springbootcashflow.adapter.presenter;

import org.delcom._1s25021springbootcashflow.domain.entity.Transaction;
import org.delcom._1s25021springbootcashflow.usecase.FinanceUseCase;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FinancePresenter {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.of("id", "ID"));

    /** Contoh: Rp 1,000,000.00 (negatif: -Rp 1,000.00). */
    public String formatMoney(long amount) {
        String sign = amount < 0 ? "-" : "";
        return sign + String.format(Locale.US, "Rp %,d.00", Math.abs(amount));
    }

    /** Contoh: [1] Gaji | Rp 5,000,000.00 | [Pemasukan] */
    public String formatRow(Transaction t) {
        return "[" + t.getId() + "] " + t.getDescription() + " | " + formatMoney(t.getAmount())
                + " | [" + t.getType().getLabel() + "]";
    }

    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    public String formatSummary(FinanceUseCase.Summary s) {
        return formatBalance(s.balance());
    }

    public String formatTime(LocalDateTime time) {
        return time == null ? "-" : TIME_FORMAT.format(time);
    }

    public String addSuccess(Transaction t) {
        return t.getType().getLabel() + " berhasil dicatat: " + formatMoney(t.getAmount());
    }

    public String updateSuccess(Transaction t) {
        return "Transaksi #" + t.getId() + " berhasil diperbarui";
    }

    public String deleteSuccess(int id) {
        return "Transaksi #" + id + " berhasil dihapus";
    }

    public String failure(String reason) {
        return "Gagal: " + reason;
    }
}
