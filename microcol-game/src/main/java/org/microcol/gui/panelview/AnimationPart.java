package org.microcol.gui.panelview;

import java.awt.Graphics2D;

/**
 * Animate one part of big animation.
 */
public interface AnimationPart {

	boolean hasNextStep();

	void nextStep();

	void paint(Graphics2D graphics, Area area);
}
