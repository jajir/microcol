package org.microcol.gui.gamepanel;

import javafx.scene.canvas.GraphicsContext;

/**
 * Animate one part of big animation.
 * <p>
 * TODO animation related unit hiding. Add ability to add unit that will be
 * ignored in drawing but just when this animation is painted. It allows to
 * chain screen scroll and unit move animation together without glitches.
 * </p>
 */
public interface Animation {

    boolean hasNextStep();

    void nextStep();

    void paint(GraphicsContext graphics, Area area);
}
