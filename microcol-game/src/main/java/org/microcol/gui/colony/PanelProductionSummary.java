package org.microcol.gui.colony;

import org.microcol.gui.util.TitledPanel;

import javafx.scene.control.Label;

/**
 * Panel summarize colony production.
 */
public final class PanelProductionSummary extends TitledPanel {

    public PanelProductionSummary() {
        super("Production summary", new Label("Production summary"));
        getStyleClass().add("production-summary");
    }

}
