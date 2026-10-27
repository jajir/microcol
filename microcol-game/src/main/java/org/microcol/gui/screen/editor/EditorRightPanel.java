package org.microcol.gui.screen.editor;

import org.microcol.gui.util.ActionButtons;
import org.microcol.gui.image.ButtonIcon;
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

    private final Button buttonTree = ActionButtons.button(ButtonIcon.TREE,
            "Tree");

    private final Button buttonField = ActionButtons.button(ButtonIcon.PLOW,
            "Field");

    private final Button buttonHill = ActionButtons.button(ButtonIcon.LAND,
            "Hill");

    private final Button buttonGrass = ActionButtons.button(ButtonIcon.LAND,
            "Grass");

    private final Button buttonOcean = ActionButtons.button(ButtonIcon.OCEAN,
            "Ocean");

    private final Button buttonMountain = ActionButtons
            .button(ButtonIcon.MOUNTAIN, "Mountain");

    private final Button buttonHighSeas = ActionButtons.button(ButtonIcon.SHIP,
            "High Seas");

    @Inject
    EditorRightPanel(final MouseOperationManager mouseOperationManager) {
        buttonTree.setOnAction(e -> mouseOperationManager.setTreeOperation());
        buttonField.setOnAction(e -> mouseOperationManager.setFieldOperation());
        buttonHill.setOnAction(e -> mouseOperationManager
                .setterrainOperation(TerrainType.HILL));
        buttonGrass.setOnAction(e -> mouseOperationManager
                .setterrainOperation(TerrainType.GRASSLAND));
        buttonOcean.setOnAction(e -> mouseOperationManager
                .setterrainOperation(TerrainType.OCEAN));
        buttonMountain.setOnAction(e -> mouseOperationManager
                .setterrainOperation(TerrainType.MOUNTAIN));
        buttonHighSeas.setOnAction(e -> mouseOperationManager
                .setterrainOperation(TerrainType.HIGH_SEA));

        mainPanel.getStyleClass().add("right");
        mainPanel.getChildren().add(buttonTree);
        mainPanel.getChildren().add(buttonField);
        mainPanel.getChildren().add(buttonHill);
        mainPanel.getChildren().add(buttonGrass);
        mainPanel.getChildren().add(buttonOcean);
        mainPanel.getChildren().add(buttonMountain);
        mainPanel.getChildren().add(buttonHighSeas);

        addTerrainButton("Plains", TerrainType.PLAINS, mouseOperationManager);
        addTerrainButton("Marsh", TerrainType.MARSH, mouseOperationManager);
        addTerrainButton("Lake", TerrainType.LAKE, mouseOperationManager);
        addTerrainButton("Prairie", TerrainType.PRAIRIE, mouseOperationManager);
        addTerrainButton("Savannah", TerrainType.SAVANNAH,
                mouseOperationManager);
        addTerrainButton("Swamp", TerrainType.SWAMP, mouseOperationManager);
        addTerrainButton("Desert", TerrainType.DESERT, mouseOperationManager);
        addTerrainButton("Tundra", TerrainType.TUNDRA, mouseOperationManager);
        addTerrainButton("Arctic", TerrainType.ARCTIC, mouseOperationManager);
    }

    private void addTerrainButton(final String label,
            final TerrainType terrainType,
            final MouseOperationManager mouseOperationManager) {
        final Button button = ActionButtons.button(ButtonIcon.LAND, label);
        button.setOnAction(
                e -> mouseOperationManager.setterrainOperation(terrainType));
        mainPanel.getChildren().add(button);
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
