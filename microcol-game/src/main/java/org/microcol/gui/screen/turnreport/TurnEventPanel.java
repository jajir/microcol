package org.microcol.gui.screen.turnreport;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Show one turn event with button to mark event as solved.
 */
public final class TurnEventPanel extends HBox {

    TurnEventPanel(final TeItem turnEvent) {
        final Label labelBullet = new Label();
        labelBullet.getStyleClass().add("bullet");
        getChildren().add(labelBullet);
        getChildren().add(new Label(turnEvent.getMessage()));
        getStyleClass().add("turn-event");
    }

}
