package org.akropia.gui.screen.turnreport;

import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

/** One borderless report row with aligned icon, message and navigation. */
public final class TurnEventPanel extends GridPane {
    TurnEventPanel(final TeItem turnEvent, final Node icon,
            final Button... actions) {
        for (double percent : new double[] { 6, 59, 35 }) {
            final ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(percent);
            getColumnConstraints().add(column);
        }
        final RowConstraints row = new RowConstraints();
        row.setValignment(VPos.TOP);
        getRowConstraints().add(row);
        final StackPane iconCell = new StackPane();
        iconCell.setAlignment(Pos.TOP_LEFT);
        if (icon != null) {
            iconCell.getChildren().add(icon);
        }
        final Label message = new Label(turnEvent.getMessage());
        message.getStyleClass().add("turn-event-message");
        message.setMinWidth(0);
        message.setMaxWidth(Double.MAX_VALUE);
        final FlowPane buttons = new FlowPane();
        buttons.getStyleClass().add("turn-event-actions");
        buttons.setMinWidth(0);
        for (Button action : actions) {
            action.setMinWidth(Region.USE_PREF_SIZE);
            buttons.getChildren().add(action);
        }
        add(iconCell, 0, 0);
        add(message, 1, 0);
        add(buttons, 2, 0);
        getStyleClass().add("turn-event");
    }
}
