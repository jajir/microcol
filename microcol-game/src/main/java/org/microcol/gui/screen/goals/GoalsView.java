package org.microcol.gui.screen.goals;

import javax.inject.Singleton;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.ScreenLifeCycle;
import org.microcol.gui.util.InGamePage;
import org.microcol.gui.util.JavaFxComponent;
import com.google.inject.Inject;
import javafx.scene.layout.Region;

/** Report content inside the shared game frame. */
@Singleton
public final class GoalsView implements JavaFxComponent, ScreenLifeCycle {
    private final InGamePage page;
    private GameScreen mainPanel;

    @Inject
    GoalsView(final InGamePage page, final GoalsButtonsPanel buttons) {
        this.page = page;
        page.setToolbar(buttons.getContent());
    }

    public void setMenuPanel(final GameScreen panel) {
        mainPanel = panel;
        panel.getContent().getStyleClass().add("report-content");
        page.setContent(panel.getContent());
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
        mainPanel.beforeShow();
    }

    @Override
    public void beforeHide() {
        mainPanel.beforeHide();
    }
}
