package org.microcol.gui.screen.editor;

import java.util.Optional;

import org.microcol.model.TerrainType;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class MouseOperationManager {

    private MouseOperation currentMouseOperation;

    @Inject
    MouseOperationManager() {
    }

    void setTreeOperation() {
        currentMouseOperation = new MouseOperationTree();
    }

    void setFieldOperation() {
        currentMouseOperation = new MouseOperationField();
    }

    void setterrainOperation(final TerrainType terrainType) {
        currentMouseOperation = new MouseOperationTerrain(terrainType);
    }

    Optional<MouseOperation> getMouseOperation() {
        return Optional.ofNullable(currentMouseOperation);
    }

}
