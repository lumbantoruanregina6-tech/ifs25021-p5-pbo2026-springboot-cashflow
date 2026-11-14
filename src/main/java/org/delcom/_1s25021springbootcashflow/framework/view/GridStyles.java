package org.delcom._1s25021springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/** Gaya grid yang konsisten di semua halaman. */
public final class GridStyles {
    private GridStyles() {
    }

    public static void apply(Grid<?> grid) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setWidthFull();
        grid.setMinHeight("320px");
        grid.getColumns().forEach(c -> c.setResizable(true));
    }
}
