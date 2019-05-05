package org.microcol.gui.screen.editor;

import org.microcol.gui.util.JavaFxComponent;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Allows to select tree and than draw then from map.
 * 
 * @author jan
 *
 */
public class TreesPanel implements JavaFxComponent {

    private final VBox mainPanel = new VBox();

    private final Button buttonDrawTree = new Button();

    TreesPanel() {
        buttonDrawTree.setText("Tree");
        mainPanel.getChildren().add(buttonDrawTree);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
