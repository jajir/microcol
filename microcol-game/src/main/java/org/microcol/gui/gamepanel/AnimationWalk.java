package org.microcol.gui.gamepanel;

import java.util.ArrayList;
import java.util.List;

import org.microcol.gui.PathPlanning;
import org.microcol.model.Location;
import org.microcol.model.Unit;

import com.google.common.base.MoreObjects;
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

    AnimationWalk(final PathPlanning pathPlanning, final Location start, final Location end,
            final Unit unit, final PaintService paintService,
            final ExcludePainting excludePainting) {
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(end);
        Preconditions.checkNotNull(paintService);
        Preconditions.checkNotNull(excludePainting);
        Preconditions.checkNotNull(start.getNeighbors().contains(end),
                "Start locations '%s' is not neighbors od end location '%s'", start, end);
        this.unit = Preconditions.checkNotNull(unit);
        excludePainting.excludeUnit(unit);
        walkParticles = new ArrayList<>();
        walkParticles.add(new AnimationWalkParticle(paintService, start, end, pathPlanning));
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("walkParticles", walkParticles)
                .add("unit", unit).toString();
    }

}
