package org.delcom._1s25021springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/** Kartu konten halaman + helper notifikasi. */
public class PageCard extends VerticalLayout {
    /** Judul kartu sebagai H2 (halaman di dalam MainLayout yang sudah punya H1). */
    public PageCard(String title) {
        this(title, false);
    }

    /** mainHeading = true: judul kartu menjadi H1 (untuk halaman tanpa MainLayout, mis. login/register). */
    public PageCard(String title, boolean mainHeading) {
        addClassName("page-card");
        setPadding(true);
        setSpacing(true);
        if (title != null && !title.isBlank()) {
            Component heading = mainHeading ? new H1(title) : new H2(title);
            heading.getElement().getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-xl)");
            add(heading);
        }
    }

    public static void success(String message) {
        Notification n = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    public static void error(String message) {
        Notification n = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}