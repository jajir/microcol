package org.microcol.gui.gamepanel;

import org.microcol.gui.PathPlanning;
import org.microcol.gui.event.model.UnitMovedController;
import org.microcol.model.event.UnitMovedStepEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Process unit moved event. It plan animations.
 */
public class UnitMovedListener {

    private final Logger logger = LoggerFactory.getLogger(UnitMovedListener.class);

    private final GamePanelView gamePanelView;

    private final PaintService paintService;

    private final ExcludePainting excludePainting;

    private final PathPlanning pathPlanning;

    private final AnimationManager animationManager;

    @Inject
    public UnitMovedListener(final UnitMovedController unitMovedController,
            final GamePanelView gamePanelView, final PaintService paintService,
            final ExcludePainting excludePainting, final PathPlanning pathPlanning,
            final AnimationManager animationManager) {
        this.gamePanelView = Preconditions.checkNotNull(gamePanelView);
        this.paintService = Preconditions.checkNotNull(paintService);
        this.excludePainting = Preconditions.checkNotNull(excludePainting);
        this.pathPlanning = Preconditions.checkNotNull(pathPlanning);
        this.animationManager = Preconditions.checkNotNull(animationManager);
        unitMovedController.addListener(event -> {
            logger.debug("Walk animation was scheduled.");
            scheduleWalkAnimation(event);
            logger.info("Walk animation was completed.");
        });
    }

    private void scheduleWalkAnimation(final UnitMovedStepEvent event) {
        gamePanelView.planScrollingAnimationToLocation(event.getStart());
        animationManager.addAnimation(
                new AnimationWalk(pathPlanning, event.getStart(), event.getEnd(), event.getUnit(),
                        paintService, excludePainting),
                animation -> excludePainting.includeUnit(event.getUnit()));
        animationManager.waitWhileRunning();
    }

}
