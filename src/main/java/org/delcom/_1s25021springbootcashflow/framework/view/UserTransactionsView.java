package org.delcom._1s25021springbootcashflow.framework.view;

import org.delcom._1s25021springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom._1s25021springbootcashflow.adapter.storage.FileStorage;
import org.delcom._1s25021springbootcashflow.domain.entity.SortOption;
import org.delcom._1s25021springbootcashflow.domain.entity.Transaction;
import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import org.delcom._1s25021springbootcashflow.framework.auth.AuthSession;
import org.delcom._1s25021springbootcashflow.usecase.FinanceUseCase;
import org.delcom._1s25021springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;

/** Khusus ADMIN: transaksi pengguna lain, read-only. */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {
    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final IUserRepository users;
    private final ComboBox<User> userSelect = new ComboBox<>("Pilih pengguna");
    private final Grid<Transaction> grid = new Grid<>();
    private final Span summary = new Span("Pilih pengguna untuk melihat transaksi.");

    public UserTransactionsView(FinanceUseCase finance, FinancePresenter presenter,
                                UserUseCase userUseCase, IUserRepository users) {
        this.finance = finance;
        this.presenter = presenter;
        this.users = users;
        setSizeFull();

        userSelect.setItems(userUseCase.listUsers());
        userSelect.setItemLabelGenerator(u -> u.getFullName() + " (@" + u.getUsername() + ")");
        userSelect.setWidth("320px");
        userSelect.addValueChangeListener(e -> load(e.getValue()));

        grid.addColumn(Transaction::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setFlexGrow(1);
        grid.addColumn(t -> presenter.formatMoney(t.getAmount())).setHeader("Jumlah")
                .setComparator(java.util.Comparator.comparingLong(Transaction::getAmount)).setAutoWidth(true);
        grid.addColumn(t -> t.getType().getLabel()).setHeader("Tipe").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(t -> {
            if (t.getProofPath() == null || t.getProofPath().isBlank()) {
                return new Span("-");
            }
            String url = FileStorage.url(t.getProofPath());
            Image thumb = new Image(url, "Bukti transaksi");
            thumb.setHeight("36px");
            thumb.getStyle().set("cursor", "zoom-in");
            thumb.addClickListener(e -> new ImagePreviewDialog(url, "Bukti Transaksi #" + t.getId()).open());
            return thumb;
        })).setHeader("Bukti").setAutoWidth(true);
        grid.addColumn(t -> presenter.formatTime(t.getCreatedAt())).setHeader("Waktu")
                .setComparator(java.util.Comparator.comparing(Transaction::getCreatedAt)).setAutoWidth(true);
        GridStyles.apply(grid);

        PageCard card = new PageCard("Transaksi Pengguna (read-only)");
        card.setSizeFull();
        card.add(userSelect, summary, grid);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(users);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!user.isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }
        event.getLocation().getQueryParameters().getSingleParameter("userId").ifPresent(raw -> {
            try {
                int id = Integer.parseInt(raw);
                userSelect.getListDataView().getItems()
                        .filter(u -> u.getId() == id).findFirst().ifPresent(userSelect::setValue);
            } catch (NumberFormatException ignored) {
                // parameter tidak valid diabaikan
            }
        });
    }

    private void load(User user) {
        if (user == null) {
            grid.setItems(List.of());
            summary.setText("Pilih pengguna untuk melihat transaksi.");
            return;
        }
        grid.setItems(finance.list(user.getId(), "", SortOption.NEWEST));
        FinanceUseCase.Summary s = finance.getSummary(user.getId());
        summary.setText(s.count() + " transaksi | Pemasukan " + presenter.formatMoney(s.income())
                + " | Pengeluaran " + presenter.formatMoney(s.expense()) + " | " + presenter.formatBalance(s.balance()));
    }
}
