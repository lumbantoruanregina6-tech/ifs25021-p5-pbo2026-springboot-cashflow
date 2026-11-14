package org.delcom._1s25021springbootcashflow.framework.view;

import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/** Kartu konten halaman + helper notifikasi. */
public class PageCard extends VerticalLayout {
    public PageCard(String title) {
        addClassName("page-card");
        setPadding(true);
        setSpacing(true);
        if (title != null && !title.isBlank()) {
            H3 heading = new H3(title);
            heading.getStyle().set("margin", "0");
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
