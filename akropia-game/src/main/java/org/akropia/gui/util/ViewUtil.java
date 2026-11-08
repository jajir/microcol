package org.akropia.gui.util;

import org.akropia.gui.preferences.DeveloperMode;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import org.akropia.gui.shortcut.KeyboardShortcuts;

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
    private final KeyboardShortcuts shortcuts;

    @Inject
    public ViewUtil(final Stage parentFrame, final DeveloperMode developerMode,
            final KeyboardShortcuts shortcuts) {
        this.shortcuts = Preconditions.checkNotNull(shortcuts);
        this.parentFrame = Preconditions.checkNotNull(parentFrame);
        this.developerMode = Preconditions.checkNotNull(developerMode);
    }

    public void installSceneShortcuts(final Scene scene) {
        DeveloperModeShortcut.install(scene, developerMode, shortcuts);
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
