package org.akropia.gui.screen.turnreport;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** One notice with keyboard-accessible navigation below its wrapping text. */
public final class TurnEventPanel extends VBox {
    TurnEventPanel(final TeItem turnEvent, final Button... actions) {
        final Label bullet = new Label();
        bullet.setManaged(false);
        bullet.setVisible(false);
        final Label message = new Label(turnEvent.getMessage());
        message.setMinWidth(0);
        message.setMaxWidth(Double.MAX_VALUE);
        getChildren().addAll(bullet, message);
        getStyleClass().add("turn-event");
        if (actions.length > 0) {
            final FlowPane buttons = new FlowPane();
            buttons.getStyleClass().add("turn-event-actions");
            for (Button action : actions) {
                action.setMinWidth(Region.USE_PREF_SIZE);
                buttons.getChildren().add(action);
            }
            getChildren().add(buttons);
        }
    }
}
