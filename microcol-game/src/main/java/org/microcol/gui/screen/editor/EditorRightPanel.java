package org.microcol.gui.screen.editor;

import org.microcol.gui.util.JavaFxComponent;
import org.microcol.model.TerrainType;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

@Singleton
public class EditorRightPanel implements JavaFxComponent {

    private final VBox mainPanel = new VBox();

    private final Button buttonTree = new Button("Tree");

    private final Button buttonField = new Button("Field");

    private final Button buttonHill = new Button("Hill");

    private final Button buttonGrass = new Button("Grass");

    private final Button buttonOcean = new Button("Ocean");

    private final Button buttonMountain = new Button("mountain");

    @Inject
    EditorRightPanel(final MouseOperationManager mouseOperationManager) {
        buttonTree.setOnAction(e -> mouseOperationManager.setTreeOperation());
        buttonField.setOnAction(e -> mouseOperationManager.setFieldOperation());
        buttonHill.setOnAction(e -> mouseOperationManager.setterrainOperation(TerrainType.HILL));
        buttonGrass
                .setOnAction(e -> mouseOperationManager.setterrainOperation(TerrainType.GRASSLAND));
        buttonOcean.setOnAction(e -> mouseOperationManager.setterrainOperation(TerrainType.OCEAN));
        buttonMountain
                .setOnAction(e -> mouseOperationManager.setterrainOperation(TerrainType.MOUNTAIN));

        mainPanel.getStyleClass().add("right");
        mainPanel.getChildren().add(buttonTree);
        mainPanel.getChildren().add(buttonField);
        mainPanel.getChildren().add(buttonHill);
        mainPanel.getChildren().add(buttonGrass);
        mainPanel.getChildren().add(buttonOcean);
        mainPanel.getChildren().add(buttonMountain);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
