package org.microcol.gui.panelview;

import org.microcol.gui.ImageProvider;
import org.microcol.gui.Point;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;

/**
 * Class animate fight between two adjacent units.
 */
public class AnimationFight implements Animation {

	private int step;

	private final Unit attacker;

	private final Unit defender;

	private final ImageProvider imageProvider;

	private final int animationSpeed;

	public AnimationFight(final Unit attacker, final Unit defender, final ImageProvider imageProvider,
			final int animationSpeed) {
		this.attacker = Preconditions.checkNotNull(attacker);
		this.defender = Preconditions.checkNotNull(defender);
		this.imageProvider = Preconditions.checkNotNull(imageProvider);
		this.animationSpeed = animationSpeed;
		// FIXME JJ at this point there should be two ships on different fields.
		// Preconditions.checkArgument(attacker.getLocation().isAdjacent(defender.getLocation()),
		// "Attacker %s should be near defender %s", attacker, defender);
	}

	@Override
	public boolean hasNextStep() {
		// FIXME JJ animation speed should be used here.
		if (animationSpeed == 1) {

		}
		;
		return step < 60;
	}

	@Override
	public void nextStep() {
		step++;
	}

	@Override
	public void paint(final GraphicsContext graphics, final Area area) {
		final Point aPoint = area.convertToPoint(attacker.getLocation());
		final Point dPoint = area.convertToPoint(defender.getLocation());
		final Point middle = aPoint.add(dPoint.substract(aPoint).divide(2));
		// TODO JJ paint animation just when at least one point is on screen.
		graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_CROSSED_SWORDS), middle.getX(), middle.getY());
	}

}