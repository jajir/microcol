package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.util.Listener;
import org.microcol.gui.util.PaintService;
import org.microcol.gui.util.PathPlanningService;
import org.microcol.model.event.UnitMovedStepStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Process unit moved event. It plan animations.
 */
@Listener
public final class UnitMovedListener {

    private final Logger logger = LoggerFactory.getLogger(UnitMovedListener.class);

    private final PaintService paintService;

    private final ExcludePainting excludePainting;

    private final PathPlanningService pathPlanningService;

    private final AnimationManager animationManager;

    @Inject
    public UnitMovedListener(final PaintService paintService, final ExcludePainting excludePainting,
            final PathPlanningService pathPlanningService,
            final AnimationManager animationManager) {
        this.paintService = Preconditions.checkNotNull(paintService);
        this.excludePainting = Preconditions.checkNotNull(excludePainting);
        this.pathPlanningService = Preconditions.checkNotNull(pathPlanningService);
        this.animationManager = Preconditions.checkNotNull(animationManager);
    }

    @Subscribe
    private void onUnitMovedStepStarted(final UnitMovedStepStartedEvent event) {
        animationManager.addAnimation(
                new AnimationWalk(pathPlanningService, event.getStart(), event.getEnd(),
                        event.getUnit(), paintService, excludePainting, event.getOrientation()),
                animation -> excludePainting.includeUnit(event.getUnit()));
        /*
         * Hold thread until animation is fully drawn.
         */
        animationManager.waitWhileRunning();
        logger.info("Animation was added.");
    }

}
