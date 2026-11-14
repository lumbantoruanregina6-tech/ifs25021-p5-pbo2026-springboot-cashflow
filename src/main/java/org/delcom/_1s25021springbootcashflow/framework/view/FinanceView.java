package org.delcom._1s25021springbootcashflow.framework.view;

import org.delcom._1s25021springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom._1s25021springbootcashflow.adapter.storage.FileStorage;
import org.delcom._1s25021springbootcashflow.domain.entity.SortOption;
import org.delcom._1s25021springbootcashflow.domain.entity.Transaction;
import org.delcom._1s25021springbootcashflow.domain.entity.TransactionType;
import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.exception.DomainException;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import org.delcom._1s25021springbootcashflow.framework.auth.AuthSession;
import org.delcom._1s25021springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;
import java.io.IOException;
import java.util.Comparator;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Transaksi | Cashflow")
public class FinanceView extends VerticalLayout {
    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final IUserRepository users;
    private final FileStorage storage;

    private final Span totalCount = new Span("0");
    private final Span totalIncome = new Span("-");
    private final Span totalExpense = new Span("-");
    private final Span balance = new Span("-");
    private final TextField search = new TextField();
    private final Select<SortOption> sort = new Select<>();
    private final Grid<Transaction> grid = new Grid<>();

    private User current;

    public FinanceView(FinanceUseCase finance, FinancePresenter presenter, IUserRepository users, FileStorage storage) {
        this.finance = finance;
        this.presenter = presenter;
        this.users = users;
        this.storage = storage;

        setSizeFull();
        add(summaryCards(), toolbar(), buildGrid());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        current = AuthSession.currentUser(users);
        if (current != null) {
            refresh();
        }
    }

    // ---------- UI ----------

    private Div summaryCards() {
        Div cards = new Div();
        cards.addClassName("summary-cards");
        cards.add(card("Total transaksi", totalCount, ""),
                card("Pemasukan", totalIncome, "income"),
                card("Pengeluaran", totalExpense, "expense"),
                card("Saldo", balance, "balance"));
        return cards;
    }

    private Div card(String title, Span value, String css) {
        Span label = new Span(title);
        label.addClassName("summary-label");
        value.addClassName("summary-value");
        Div card = new Div(label, value);
        card.addClassNames("summary-card", css);
        return card;
    }

    private HorizontalLayout toolbar() {
        search.setPlaceholder("Cari keterangan...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setClearButtonVisible(true);
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(e -> refresh());

        sort.setItems(SortOption.values());
        sort.setItemLabelGenerator(SortOption::getLabel);
        sort.setValue(SortOption.NEWEST);
        sort.addValueChangeListener(e -> refresh());

        Button reload = new Button("Muat ulang", VaadinIcon.REFRESH.create(), e -> refresh());
        Button income = new Button("Pemasukan", VaadinIcon.PLUS.create(), e -> openForm(TransactionType.INCOME, null));
        income.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        Button expense = new Button("Pengeluaran", VaadinIcon.MINUS.create(), e -> openForm(TransactionType.EXPENSE, null));
        expense.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout bar = new HorizontalLayout(search, sort, reload, income, expense);
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.END);
        bar.setFlexWrap(true);
        return bar;
    }

    private Grid<Transaction> buildGrid() {
        grid.addColumn(Transaction::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(t -> {
            Span s = new Span(presenter.formatMoney(t.getAmount()));
            s.getStyle().set("font-weight", "600").set("color", t.getType() == TransactionType.INCOME
                    ? "var(--lumo-success-text-color)" : "var(--lumo-error-text-color)");
            return s;
        })).setHeader("Jumlah").setComparator(Comparator.comparingLong(Transaction::getAmount)).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(t -> {
            Span badge = new Span(t.getType().getLabel());
            badge.getElement().getThemeList().add("badge " + (t.getType() == TransactionType.INCOME ? "success" : "error"));
            return badge;
        })).setHeader("Tipe").setComparator(Comparator.comparing(Transaction::getType)).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(t -> {
            if (t.getProofPath() == null || t.getProofPath().isBlank()) {
                return new Span("-");
            }
            String url = FileStorage.url(t.getProofPath());
            Image thumb = new Image(url, "Bukti transaksi");
            thumb.setHeight("36px");
            thumb.getStyle().set("cursor", "zoom-in").set("border-radius", "4px");
            thumb.addClickListener(e -> new ImagePreviewDialog(url, "Bukti Transaksi #" + t.getId()).open());
            return thumb;
        })).setHeader("Bukti").setAutoWidth(true);
        grid.addColumn(t -> presenter.formatTime(t.getCreatedAt())).setHeader("Waktu")
                .setComparator(Comparator.comparing(Transaction::getCreatedAt)).setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(t -> {
            Button edit = new Button(VaadinIcon.EDIT.create(), e -> openForm(t.getType(), t));
            edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            edit.setAriaLabel("Ubah");
            Button delete = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(t));
            delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            delete.setAriaLabel("Hapus");
            return new HorizontalLayout(edit, delete);
        })).setHeader("Aksi").setAutoWidth(true).setFlexGrow(0);
        GridStyles.apply(grid);
        return grid;
    }

    // ---------- data ----------

    private void refresh() {
        if (current == null) return;
        grid.setItems(finance.list(current.getId(), search.getValue(), sort.getValue()));
        FinanceUseCase.Summary s = finance.getSummary(current.getId());
        totalCount.setText(String.valueOf(s.count()));
        totalIncome.setText(presenter.formatMoney(s.income()));
        totalExpense.setText(presenter.formatMoney(s.expense()));
        balance.setText(presenter.formatMoney(s.balance()));
    }

    // ---------- dialog ----------

    private void openForm(TransactionType type, Transaction existing) {
        boolean edit = existing != null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(edit ? "Ubah Transaksi #" + existing.getId() : "Tambah " + type.getLabel());
        dialog.setWidth("440px");
        dialog.setMaxWidth("95vw");

        TextField description = new TextField("Keterangan");
        description.setWidthFull();
        TextField amount = new TextField("Jumlah (Rp)");
        amount.setPlaceholder("contoh: 150000");
        amount.setWidthFull();
        if (edit) {
            description.setValue(existing.getDescription());
            amount.setValue(String.valueOf(existing.getAmount()));
        }

        String[] fileName = new String[1];
        byte[][] fileData = new byte[1][];
        UI ui = UI.getCurrent();
        Span uploadInfo = new Span(edit ? "Unggah untuk mengganti bukti (opsional)" : "Bukti gambar (opsional)");
        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            fileName[0] = metadata.fileName();
            fileData[0] = data;
            ui.access(() -> uploadInfo.setText("Terpilih: " + metadata.fileName()));
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        dialog.add(new VerticalLayout(description, amount, uploadInfo, upload, progress));

        Button cancel = new Button("Batal", e -> dialog.close());
        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            String proof = null;
            try {
                if (fileData[0] != null) {
                    proof = storage.store(fileName[0], fileData[0]);
                }
                Transaction saved;
                if (edit) {
                    saved = finance.update(current.getId(), existing.getId(), description.getValue(), amount.getValue(), proof);
                } else if (type == TransactionType.INCOME) {
                    saved = finance.addIncome(current.getId(), description.getValue(), amount.getValue(), proof);
                } else {
                    saved = finance.addExpense(current.getId(), description.getValue(), amount.getValue(), proof);
                }
                PageCard.success(edit ? presenter.updateSuccess(saved) : presenter.addSuccess(saved));
                dialog.close();
                refresh();
            } catch (DomainException ex) {
                discard(proof);
                PageCard.error(presenter.failure(ex.getMessage()));
            } catch (IOException ex) {
                PageCard.error(presenter.failure("file bukti tidak dapat disimpan"));
            } finally {
                progress.setVisible(false);
                save.setEnabled(true);
            }
        });
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void confirmDelete(Transaction t) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Hapus Transaksi");
        dialog.add(new Span("Hapus " + presenter.formatRow(t) + " ?"));
        Button cancel = new Button("Batal", e -> dialog.close());
        Button delete = new Button("Hapus");
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        delete.addClickListener(e -> {
            try {
                finance.delete(current.getId(), t.getId());
                PageCard.success(presenter.deleteSuccess(t.getId()));
                dialog.close();
                refresh();
            } catch (DomainException ex) {
                PageCard.error(presenter.failure(ex.getMessage()));
            }
        });
        dialog.getFooter().add(cancel, delete);
        dialog.open();
    }

    private void discard(String proof) {
        try {
            storage.delete(proof);
        } catch (IOException ignored) {
            // file yatim tidak kritis
        }
    }
}
