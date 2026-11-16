package org.delcom._1s25021springbootcashflow.framework.view;

import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.exception.DomainException;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import org.delcom._1s25021springbootcashflow.framework.auth.AuthSession;
import org.delcom._1s25021springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
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

@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthUseCase auth;
    private final IUserRepository users;

    public LoginView(AuthUseCase auth, IUserRepository users) {
        this.auth = auth;
        this.users = users;

        setSizeFull();
        getElement().setAttribute("role", "main");
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        TextField username = new TextField("Username");
        username.setWidthFull();
        username.setAutofocus(true);
        username.addAttachListener(event -> assignIdToInput(username, "login-username"));

        PasswordField password = new PasswordField("Password");
        password.setWidthFull();
        password.addAttachListener(event -> assignIdToInput(password, "login-password"));

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);

        Button login = new Button("Masuk");
        login.setId("login-submit");
        login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        login.setWidthFull();
        login.addClickShortcut(Key.ENTER);
        login.addClickListener(e -> {
            progress.setVisible(true);
            login.setEnabled(false);
            try {
                User user = this.auth.login(username.getValue(), password.getValue());
                AuthSession.login(user);
                UI.getCurrent().navigate(FinanceView.class);
            } catch (DomainException ex) {
                PageCard.error(ex.getMessage());
            } finally {
                progress.setVisible(false);
                login.setEnabled(true);
            }
        });

        PageCard card = new PageCard("Masuk ke Cashflow");
        card.setWidth("380px");
        card.setMaxWidth("95vw");
        card.add(username, password, progress, login, new RouterLink("Belum punya akun? Daftar", RegisterView.class));
        add(card);
    }

    /** Memindahkan id dari pembungkus Vaadin ke elemen input sungguhan di dalamnya. */
    private static void assignIdToInput(Component field, String id) {
        field.getElement().executeJs(
                "const host = this;"
                        + "const apply = () => { const input = host.querySelector('input'); if (!input) { return false; }"
                        + " input.id = $0; const label = host.querySelector('label'); if (label) { label.htmlFor = $0; } return true; };"
                        + "if (!apply()) { const timer = setInterval(() => { if (apply()) { clearInterval(timer); } }, 50);"
                        + " setTimeout(() => clearInterval(timer), 5000); }", id);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) != null) {
            event.forwardTo(FinanceView.class);
        }
    }
}