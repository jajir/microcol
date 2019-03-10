package org.microcol.gui.dock;

import java.util.List;

import org.microcol.gui.util.JavaFxComponent;

import javafx.scene.layout.HBox;

public final class PanelDockCratesView implements JavaFxComponent {

    private final HBox mainBox;

    PanelDockCratesView(final List<PanelCratePresenter> dockCrates) {
        mainBox = new HBox();
        dockCrates.forEach(paneCrate -> {
            paneCrate.setIsClosed(true);
            mainBox.getChildren().add(paneCrate.getContent());
        });
    }

    @Override
    public HBox getContent() {
        return mainBox;
    }
}
