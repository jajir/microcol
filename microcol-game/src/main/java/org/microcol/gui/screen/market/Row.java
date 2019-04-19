package org.microcol.gui.screen.market;

import org.microcol.gui.util.JavaFxComponent;

import com.google.common.base.Preconditions;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Contains simple table row. Each column have class 'column0', 'column1', ...
 */
public class Row implements JavaFxComponent {

    private final HBox row = new HBox();

    public Row() {

    }

    public Row(final Node... nodes) {
        for (final Node node : nodes) {
            add(node);
        }
    }

    @Override
    public Region getContent() {
        return row;
    }

    public void add(final Node node) {
        Preconditions.checkNotNull(node);
        node.getStyleClass().add("column" + row.getChildren().size());
        row.getChildren().add(node);
    }

}
