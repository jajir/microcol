package org.microcol.gui.panelview;

import javafx.scene.canvas.GraphicsContext;

/**
 * Animate one part of big animation.
 */
public interface Animation {

	boolean hasNextStep();

	void nextStep();

	void paint(GraphicsContext graphics, Area area);
}
