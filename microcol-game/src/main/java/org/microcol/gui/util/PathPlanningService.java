package org.microcol.gui.util;

import java.util.List;

import org.microcol.gui.Point;
import org.microcol.gui.preferences.GamePreferences;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Class contains methods for computing path for units.
 * <p>
 * Animation speed is limited by {@link #ANIMATION_SPEED_MIN_VALUE} and by
 * {@link #ANIMATION_SPEED_MAX_VALUE}.
 * </p>
 */
@Singleton
public class PathPlanningService {

    private final GamePreferences gamePreferences;

    /**
     * Minimal value of animation speed.
     */
    public static final int ANIMATION_SPEED_MIN_VALUE = 0;

    /**
     * Maximal value of animation speed.
     */
    public static final int ANIMATION_SPEED_MAX_VALUE = 5;

    private final PathPlanning pathPlanning;

    @Inject
    public PathPlanningService(final GamePreferences gamePreferences,
            final PathPlanning pathPlanning) {
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
    }

    public List<Point> getPathLimitSpeed(final Point tileFrom, final Point tileTo) {
        return pathPlanning.getPathLimitSpeed(tileFrom, tileTo,
                gamePreferences.getAnimationSpeed());

    }

    public static void checkSpeed(final int speed) {
        Preconditions.checkArgument(speed >= ANIMATION_SPEED_MIN_VALUE, "speed '%s' is to low",
                speed);
        Preconditions.checkArgument(speed < ANIMATION_SPEED_MAX_VALUE, "speed '%s' is to high",
                speed);
    }

}
