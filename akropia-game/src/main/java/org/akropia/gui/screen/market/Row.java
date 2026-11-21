package org.akropia.gui.screen.market;

import org.akropia.gui.util.JavaFxComponent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/** Aligned caption and amount in a market quote. */
public final class Row implements JavaFxComponent {
    private final HBox row;

    Row(final Label caption, final Label value) {
        caption.setWrapText(true);
        caption.setMinWidth(0);
        caption.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(caption, Priority.ALWAYS);
        value.setAlignment(Pos.CENTER_RIGHT);
        value.setMinWidth(140);
        value.setPrefWidth(140);
        row = new HBox(16, caption, value);
        row.setAlignment(Pos.BASELINE_LEFT);
        row.getStyleClass().add("market-price-row");
    }

    @Override
    public Region getContent() {
        return row;
    }
}
