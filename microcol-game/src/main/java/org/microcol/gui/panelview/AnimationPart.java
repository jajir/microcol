package org.microcol.gui.panelview;

import java.awt.Graphics2D;

/**
 * Animate one part of big animation.
 */
public abstract class AnimationPart {

	public abstract boolean hasNextStep();

	public abstract void nextStep();

	public abstract void paint(Graphics2D graphics, Area area);
}
