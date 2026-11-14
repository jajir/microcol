package org.akropia.gui.util;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/** Final dialog actions, with the primary action last. */
public final class ModalActions extends HBox {
    public ModalActions(final Button primary, final Button... preceding) {
        super(12);
        setAlignment(Pos.CENTER_RIGHT);
        setMinWidth(Region.USE_PREF_SIZE);
        getStyleClass().add("modal-actions");
        getChildren().addAll(preceding);
        if (primary != null) {
            primary.getStyleClass().add("modal-primary");
            getChildren().add(primary);
        }
        for (final javafx.scene.Node node : getChildren()) {
            final Button button = (Button) node;
            button.setMinWidth(Region.USE_PREF_SIZE);
        }
    }
}
