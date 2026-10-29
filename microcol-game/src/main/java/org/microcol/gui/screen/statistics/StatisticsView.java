package org.microcol.gui.screen.statistics;

import javax.inject.Singleton;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.ScreenLifeCycle;
import org.microcol.gui.util.InGamePage;
import org.microcol.gui.util.JavaFxComponent;
import com.google.inject.Inject;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

/** Report content inside the shared game frame. */
@Singleton
public final class StatisticsView implements JavaFxComponent, ScreenLifeCycle {
    private final InGamePage page;
    private GameScreen mainPanel;

    @Inject
    StatisticsView(final InGamePage page,
            final StatisticsButtonsPanel buttons) {
        this.page = page;
        page.getContent().getStyleClass().add("statistics-atlas");
        page.setToolbar(buttons.getContent());
    }

    public void setMenuPanel(final StatisticsPanel panel) {
        mainPanel = panel;
        page.setTitle(panel.getTitle());
        panel.getContent().getStyleClass().add("report-content");
        page.setContent(panel.getContent());
        VBox.setVgrow(panel.getContent(), Priority.ALWAYS);
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
