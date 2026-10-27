package org.microcol.gui.util;

import org.microcol.gui.preferences.DeveloperMode;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.stage.Stage;
import javafx.scene.Scene;

/**
 * Provide the main stage, screen center and shared scene shortcuts.
 */
public final class ViewUtil {

    /**
     * All game dialogs, windows and UI features are children of this main
     * {@link javafx.stage.Stage}.
     */
    private final Stage parentFrame;
    private final DeveloperMode developerMode;

    @Inject
    public ViewUtil(final Stage parentFrame,
            final DeveloperMode developerMode) {
        this.parentFrame = Preconditions.checkNotNull(parentFrame);
        this.developerMode = Preconditions.checkNotNull(developerMode);
    }

    public void installSceneShortcuts(final Scene scene) {
        DeveloperModeShortcut.install(scene, developerMode);
    }

    public Stage getPrimaryStage() {
        return parentFrame;
    }

    public double getPrimaryStageCenterXPosition() {
        return parentFrame.getX() + parentFrame.getWidth() / 2d;
    }

    public double getPrimaryStageCenterYPosition() {
        return parentFrame.getY() + parentFrame.getHeight() / 2d;
    }

}
