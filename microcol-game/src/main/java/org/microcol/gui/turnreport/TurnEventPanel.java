package org.microcol.gui.turnreport;

import org.microcol.model.turnevent.TurnEvent;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * Show one turn event with button to mark event as solved.
 */
public final class TurnEventPanel extends HBox {

    TurnEventPanel(final TurnEvent turnEvent) {
        getChildren().add(new Label(turnEvent.getLocalizedMessage()));
    }

}
