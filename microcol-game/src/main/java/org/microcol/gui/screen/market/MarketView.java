package org.microcol.gui.screen.market;

import org.microcol.gui.screen.ScreenLifeCycle;
import org.microcol.gui.screen.menu.TitledPage;
import org.microcol.gui.util.CenteredPage;
import org.microcol.gui.util.JavaFxComponent;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;

/**
 * Class will connect background and centered page and title.
 */
public final class MarketView implements JavaFxComponent, ScreenLifeCycle {

    private final TitledPage titledPage;

    private final CenteredPage centeredPage;

    private final MarketBackground background;

    @Inject
    MarketView(final MarketBackground background, final TitledPage titledPage,
            final CenteredPage centeredPages) {
        this.background = Preconditions.checkNotNull(background);
        this.centeredPage = Preconditions.checkNotNull(centeredPages);
        this.titledPage = Preconditions.checkNotNull(titledPage);

        centeredPages.setBackground(background);
        centeredPages.setMainPanel(titledPage);
    }

    public void setMenuPanel(final JavaFxComponent menuPanel) {
        Preconditions.checkNotNull(menuPanel);
        titledPage.setContent(menuPanel);
    }

    public void setTitle(final String menuTitle) {
        titledPage.setTitle(menuTitle);
    }

    @Override
    public Region getContent() {
        return centeredPage.getContent();
    }

    @Override
    public void beforeShow() {
        background.beforeShow();
    }

    @Override
    public void beforeHide() {
        background.beforeHide();
    }

}
