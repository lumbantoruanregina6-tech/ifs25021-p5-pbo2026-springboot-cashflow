package org.delcom._1s25021springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/** Konfigurasi indikator loading global Vaadin yang responsif. */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {
    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            var config = uiEvent.getUI().getLoadingIndicatorConfiguration();
            config.setFirstDelay(150);
            config.setSecondDelay(600);
            config.setThirdDelay(1500);

            // Tandai indikator koneksi sebagai live region agar dianggap bagian dari landmark (aksesibilitas)
            uiEvent.getUI().getPage().executeJs(
                    "const apply = () => {"
                            + " const el = document.querySelector('vaadin-connection-indicator');"
                            + " if (!el) { return false; }"
                            + " el.setAttribute('role', 'status');"
                            + " el.setAttribute('aria-live', 'polite');"
                            + " return true; };"
                            + "if (!apply()) {"
                            + " const obs = new MutationObserver(() => { if (apply()) { obs.disconnect(); } });"
                            + " obs.observe(document.documentElement, { childList: true, subtree: true });"
                            + " setTimeout(() => obs.disconnect(), 10000); }");
        });
    }
}