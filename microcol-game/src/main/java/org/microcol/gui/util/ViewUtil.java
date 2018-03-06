package org.microcol.gui.util;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.stage.Stage;

/**
 * Provide game main stage and provide screen center point.
 */
public class ViewUtil {

    /**
     * All game dialogs, windows and UI features are children of this main
     * {@link javafx.stage.Stage}.
     */
    private final Stage parentFrame;

    @Inject
    public ViewUtil(final Stage parentFrame) {
        this.parentFrame = Preconditions.checkNotNull(parentFrame);
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
