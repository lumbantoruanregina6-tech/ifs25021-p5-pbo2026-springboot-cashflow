package org.delcom._1s25021springbootcashflow.framework.view;

import org.delcom._1s25021springbootcashflow.adapter.storage.FileStorage;
import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import org.delcom._1s25021springbootcashflow.framework.auth.AuthSession;
import org.delcom._1s25021springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import java.util.Map;

/** Khusus ADMIN: daftar semua pengguna. */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {
    private final UserUseCase userUseCase;
    private final IUserRepository users;
    private final Grid<User> grid = new Grid<>();

    public UsersView(UserUseCase userUseCase, IUserRepository users) {
        this.userUseCase = userUseCase;
        this.users = users;
        setSizeFull();

        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(new ComponentRenderer<>(u -> {
            Avatar a = new Avatar(u.getFullName());
            if (u.getPhotoPath() != null && !u.getPhotoPath().isBlank()) {
                a.setImage(FileStorage.url(u.getPhotoPath()));
            }
            return a;
        })).setHeader("Foto").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(User::getUsername).setHeader("Username");
        grid.addColumn(User::getFullName).setHeader("Nama lengkap");
        grid.addColumn(u -> u.getRole().name()).setHeader("Role");
        grid.addColumn(new ComponentRenderer<>(u -> {
            Button view = new Button("Lihat transaksi", e -> UI.getCurrent().navigate(UserTransactionsView.class,
                    QueryParameters.simple(Map.of("userId", String.valueOf(u.getId())))));
            view.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return view;
        })).setHeader("Aksi").setAutoWidth(true);
        GridStyles.apply(grid);

        PageCard card = new PageCard("Daftar Pengguna");
        card.setSizeFull();
        card.add(grid);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(users);
        if (user == null) {
            event.forwardTo(LoginView.class);
        } else if (!user.isAdmin()) {
            event.forwardTo(FinanceView.class);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        grid.setItems(userUseCase.listUsers());
    }
}
