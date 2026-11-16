package org.delcom._1s25021springbootcashflow.framework.view;

import org.delcom._1s25021springbootcashflow.domain.entity.User;
import org.delcom._1s25021springbootcashflow.domain.repository.IUserRepository;
import org.delcom._1s25021springbootcashflow.adapter.storage.FileStorage;
import org.delcom._1s25021springbootcashflow.framework.auth.AuthSession;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

/** Layout aplikasi: sidebar, avatar menu, dan guard login global. */
public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private final IUserRepository users;
    private Integer builtForUserId;

    public MainLayout(IUserRepository users) {
        this.users = users;
        setPrimarySection(Section.DRAWER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(users);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (builtForUserId == null || builtForUserId != user.getId()) {
            build(user);
            builtForUserId = user.getId();
        }
    }

    /** Tandai konten halaman sebagai landmark utama (aksesibilitas: main landmark). */
    @Override
    public void showRouterLayoutContent(HasElement content) {
        super.showRouterLayoutContent(content);
        if (content != null) {
            content.getElement().setAttribute("role", "main");
        }
    }

    private void build(User user) {
        H1 title = new H1("Cashflow");
        title.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title, avatarMenu(user));
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding", "0 var(--lumo-space-m)");
        header.expand(title);
        addToNavbar(header);

        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Transaksi", FinanceView.class, VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
            nav.addItem(new SideNavItem("Transaksi Pengguna", UserTransactionsView.class, VaadinIcon.LIST.create()));
        }
        addToDrawer(nav);
    }

    private MenuBar avatarMenu(User user) {
        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null && !user.getPhotoPath().isBlank()) {
            avatar.setImage(FileStorage.url(user.getPhotoPath()));
        }
        MenuBar menu = new MenuBar();
        menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        var item = menu.addItem(avatar);
        item.getSubMenu().addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        item.getSubMenu().addItem("Keluar", e -> {
            AuthSession.logout();
            UI.getCurrent().getPage().setLocation("/login");
        });
        return menu;
    }
}