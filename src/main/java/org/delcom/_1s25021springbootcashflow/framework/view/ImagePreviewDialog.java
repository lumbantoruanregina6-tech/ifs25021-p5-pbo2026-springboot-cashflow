package org.delcom._1s25021springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;

/** Modal preview gambar. Klik gambar = toggle zoom 1.0x / 1.8x; tombol Perkecil, Perbesar, Tutup. */
public class ImagePreviewDialog extends Dialog {
    private static final double MIN = 0.5;
    private static final double MAX = 3.0;
    private static final double STEP = 0.2;

    private final Image image;
    private double scale = 1.0;

    public ImagePreviewDialog(String url, String title) {
        setHeaderTitle(title);
        setWidth("720px");
        setMaxWidth("95vw");

        image = new Image(url, "Pratinjau gambar");
        image.getStyle().set("max-width", "none").set("cursor", "zoom-in").set("display", "block");
        image.addClickListener(e -> setScale(scale > 1.0 ? 1.0 : 1.8));

        Div viewport = new Div(image);
        viewport.getStyle().set("overflow", "auto").set("max-height", "60vh").set("text-align", "center");
        add(viewport);

        Button smaller = new Button("Perkecil", e -> setScale(Math.max(MIN, scale - STEP)));
        Button bigger = new Button("Perbesar", e -> setScale(Math.min(MAX, scale + STEP)));
        Button close = new Button("Tutup", e -> close());
        getFooter().add(smaller, bigger, close);
        setScale(1.0);
    }

    private void setScale(double value) {
        scale = value;
        image.setWidth(Math.round(scale * 100) + "%");
        image.getStyle().set("cursor", scale > 1.0 ? "zoom-out" : "zoom-in");
    }
}
