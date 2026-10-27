package org.microcol.gui.screen.market;

import org.microcol.gui.screen.ScreenLifeCycle;
import org.microcol.gui.util.InGamePage;
import org.microcol.gui.util.JavaFxComponent;
import com.google.inject.Inject;
import javafx.scene.layout.Region;

/** European trade uses the same frame and live balance as the port. */
public final class MarketView implements JavaFxComponent, ScreenLifeCycle {
    private final InGamePage page;

    @Inject
    MarketView(final InGamePage page) {
        this.page = page;
    }

    public void setMenuPanel(final JavaFxComponent panel) {
        panel.getContent().getStyleClass().add("report-content");
        page.setContent(panel.getContent());
    }

    public void setToolbar(final Region toolbar) {
        page.setToolbar(toolbar);
    }

    public void setTitle(final String title) {
        page.setTitle(title);
    }

    @Override
    public Region getContent() {
        return page.getContent();
    }

    @Override
    public void beforeShow() {
        page.beforeShow();
    }

    @Override
    public void beforeHide() {
    }
}
