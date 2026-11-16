package org.delcom._1s25021springbootcashflow.framework.view;

import org.delcom._1s25021springbootcashflow.domain.exception.DomainException;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import org.delcom._1s25021springbootcashflow.framework.auth.AuthSession;
import org.delcom._1s25021springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("register")
@PageTitle("Daftar | Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {
    private final IUserRepository users;

    public RegisterView(AuthUseCase auth, IUserRepository users) {
        this.users = users;

        setSizeFull();
        getElement().setAttribute("role", "main");
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        TextField fullName = new TextField("Nama lengkap");
        fullName.setWidthFull();
        TextField username = new TextField("Username");
        username.setWidthFull();
        PasswordField password = new PasswordField("Password");
        password.setWidthFull();
        PasswordField confirm = new PasswordField("Ulangi password");
        confirm.setWidthFull();

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button register = new Button("Daftar");
        register.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        register.setWidthFull();
        register.addClickListener(e -> {
            if (!password.getValue().equals(confirm.getValue())) {
                PageCard.error("Konfirmasi password tidak sama");
                return;
            }
            progress.setVisible(true);
            register.setEnabled(false);
            try {
                auth.register(username.getValue(), password.getValue(), fullName.getValue());
                PageCard.success("Registrasi berhasil, silakan masuk");
                UI.getCurrent().navigate(LoginView.class);
            } catch (DomainException ex) {
                PageCard.error(ex.getMessage());
            } finally {
                progress.setVisible(false);
                register.setEnabled(true);
            }
        });

        PageCard card = new PageCard("Daftar Akun Cashflow");
        card.setWidth("380px");
        card.setMaxWidth("95vw");
        card.add(fullName, username, password, confirm, progress, register,
                new RouterLink("Sudah punya akun? Masuk", LoginView.class));
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) != null) {
            event.forwardTo(FinanceView.class);
        }
    }
}