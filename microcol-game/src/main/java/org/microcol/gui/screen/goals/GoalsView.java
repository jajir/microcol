package org.microcol.gui.screen.goals;

import javax.inject.Singleton;

import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.ScreenLifeCycle;
import org.microcol.gui.screen.turnreport.TurnReportBackground;
import org.microcol.gui.util.ButtonedPage;
import org.microcol.gui.util.CenteredPage;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.TitledPage;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.layout.Region;

/**
 * Class will connect background and centered page and title.
 */
@Singleton
public final class GoalsView implements JavaFxComponent, ScreenLifeCycle {

    private final TitledPage titledPage;

    private final CenteredPage centeredPage;

    private final TurnReportBackground background;

    private GameScreen mainPanel;

    @Inject
    GoalsView(final TurnReportBackground background, final TitledPage titledPage,
            final CenteredPage centeredPages, final GoalsButtonsPanel goalsButtonsPanel) {
        this.background = Preconditions.checkNotNull(background);
        this.centeredPage = Preconditions.checkNotNull(centeredPages);
        this.titledPage = Preconditions.checkNotNull(titledPage);

        final ButtonedPage buttonedPage = new ButtonedPage(goalsButtonsPanel);
        buttonedPage.setContent(titledPage);
        
        centeredPages.setBackground(background);
        centeredPages.setMainPanel(buttonedPage);
    }

    public void setMenuPanel(final GameScreen menuPanel) {
        this.mainPanel = Preconditions.checkNotNull(menuPanel);
        titledPage.setContent(mainPanel);
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
        mainPanel.beforeShow();
    }

    @Override
    public void beforeHide() {
        background.beforeHide();
        mainPanel.beforeHide();
    }

}
