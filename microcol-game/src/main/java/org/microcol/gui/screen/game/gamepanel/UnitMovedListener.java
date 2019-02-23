package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.PathPlanning;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.PaintService;
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

    private final PathPlanning pathPlanning;

    private final AnimationManager animationManager;

    @Inject
    public UnitMovedListener(final PaintService paintService, final ExcludePainting excludePainting,
            final PathPlanning pathPlanning, final AnimationManager animationManager) {
        this.paintService = Preconditions.checkNotNull(paintService);
        this.excludePainting = Preconditions.checkNotNull(excludePainting);
        this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
        this.animationManager = Preconditions.checkNotNull(animationManager);
    }

    @Subscribe
    private void onUnitMovedStepStarted(final UnitMovedStepStartedEvent event) {
        animationManager.addAnimation(
                new AnimationWalk(pathPlanning, event.getStart(), event.getEnd(), event.getUnit(),
                        paintService, excludePainting, event.getOrientation()),
                animation -> excludePainting.includeUnit(event.getUnit()));
        /*
         * Hold thread until animation is fully drawn.
         */
        animationManager.waitWhileRunning();
        logger.info("Animation was added.");
    }

}
