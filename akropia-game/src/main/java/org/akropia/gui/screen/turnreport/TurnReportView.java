package org.akropia.gui.screen.turnreport;

import javax.inject.Singleton;
import org.akropia.gui.screen.GameScreen;
import org.akropia.gui.screen.ScreenLifeCycle;
import org.akropia.gui.util.InGamePage;
import org.akropia.gui.util.JavaFxComponent;
import com.google.inject.Inject;
import javafx.scene.layout.Region;
import javafx.scene.control.ScrollPane;
import javafx.application.Platform;

/** Report content inside the shared game frame. */
@Singleton
public final class TurnReportView implements JavaFxComponent, ScreenLifeCycle {
    private final InGamePage page;
    private GameScreen mainPanel;
    private double scrollPosition;

    @Inject
    TurnReportView(final InGamePage page,
            final TurnReportButtonsPanel buttons) {
        this.page = page;
        page.getContent().getStyleClass().add("turn-report-screen");
        page.setToolbar(buttons.getContent());
        page.getContent().sceneProperty()
                .addListener((value, oldScene, scene) -> {
                    if (scene != null) {
                        Platform.runLater(() -> {
                            if (page.getContent().getScene() == scene) {
                                // Attaching a rebuilt list otherwise focuses
                                // its first action.
                                page.getContent().requestFocus();
                                page.getContent().applyCss();
                                page.getContent().layout();
                                scrollPane().setVvalue(scrollPosition);
                            }
                        });
                    }
                });
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

    void resetScrollPosition() {
        scrollPosition = 0;
    }

    private ScrollPane scrollPane() {
        return (ScrollPane) page.getContent().lookup(".screen-scroll");
    }

    @Override
    public void beforeHide() {
        scrollPosition = scrollPane().getVvalue();
        mainPanel.beforeHide();
    }
}
