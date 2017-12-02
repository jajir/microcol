package org.microcol.gui.gamepanel;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.PathPlanning;
import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;

import javafx.scene.canvas.GraphicsContext;

/**
 * Draw walk animation based on predefined path.
 * 
 */
public class AnimationWalk implements Animation {

	/**
	 * Moving unit.
	 */
	private final Unit unit;

	private final List<AnimationWalkParticle> walkParticles;

	public AnimationWalk(final PathPlanning pathPlanning, final List<Location> path, final Unit unit,
			final PaintService paintService, final ExcludePainting excludePainting) {
		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(!path.isEmpty(), "Path can't be empty");
		Preconditions.checkArgument(path.size() > 1, "Path should contains more than one locations");
		this.unit = Preconditions.checkNotNull(unit);
		excludePainting.excludeUnit(unit);
		walkParticles = new ArrayList<>();
		Location previous = null;
		for (final Location loc : path) {
			if (previous != null) {
				walkParticles.add(new AnimationWalkParticle(paintService, previous, loc, pathPlanning));
			}
			previous = loc;
		}
		Preconditions.checkArgument(hasNextStep(), "Animation can't start without any steps.");
	}

	@Override
	public void nextStep() {
		if (!walkParticles.isEmpty() && !walkParticles.get(0).hasNextStep()) {
			walkParticles.remove(0);
		}
	}

	/**
	 * Provide information if animation should continue.
	 * 
	 * @return return <code>true</code> when not all animation was drawn, it
	 *         return <code>false</code> when all animation is done
	 */
	@Override
	public boolean hasNextStep() {
		return !walkParticles.isEmpty() && walkParticles.get(0).hasNextStep();
	}

	@Override
	public void paint(final GraphicsContext graphics, final Area area) {
		AnimationWalkParticle particle = walkParticles.get(0);
		particle.paint(graphics, area, unit);
	}

}
