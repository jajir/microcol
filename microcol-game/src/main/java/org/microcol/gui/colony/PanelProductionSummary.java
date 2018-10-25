package org.microcol.gui.colony;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.gui.util.TitledPanel;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/**
 * Panel summarize colony production.
 */
public final class PanelProductionSummary implements JavaFxComponent {

    private final TitledPanel mainPanel;

    public PanelProductionSummary() {
        mainPanel = new TitledPanel("Production summary", new Label("Production summary"));
        mainPanel.getStyleClass().add("production-summary");
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
