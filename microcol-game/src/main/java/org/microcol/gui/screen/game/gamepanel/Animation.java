package org.microcol.gui.screen.game.gamepanel;

import javafx.scene.canvas.GraphicsContext;

/**
 * Animate one part of big animation.
 */
public interface Animation {

    boolean hasNextStep();

    void nextStep();

    void paint(GraphicsContext graphics, Area area);

    boolean canBePainted(Area area);
}
