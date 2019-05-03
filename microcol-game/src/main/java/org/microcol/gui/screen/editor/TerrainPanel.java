package org.microcol.gui.screen.editor;

import org.microcol.gui.util.JavaFxComponent;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class TerrainPanel implements JavaFxComponent {

    private final VBox mainPanel = new VBox();

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
