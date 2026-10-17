package org.microcol.gui.screen.turnreport;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

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

    /** Show the message followed by its focusable follow-up action. */
    TurnEventPanel(final TeItem turnEvent, final Button action) {
        this(turnEvent);
        setSpacing(10);
        setAlignment(Pos.CENTER_LEFT);
        // A narrower message keeps the whole action label inside the report.
        getStyleClass().add("turn-event-action");
        action.setMinWidth(Region.USE_PREF_SIZE);
        getChildren().add(action);
    }

}
