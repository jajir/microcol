package org.microcol.gui.screen.game.gamepanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.microcol.gui.event.StartMoveEvent;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.UnitUtil;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Class helps with drawing of action path.
 * <p>
 * Class works with Locations because point could change without moving over
 * different tile. (screen scrolling)
 * </p>
 */
@Listener
public final class MoveModeSupport {

    private final Logger logger = LoggerFactory.getLogger(MoveModeSupport.class);

    private final SelectedTileManager selectedTileManager;

    private final MouseOverTileManager mouseOverTileManager;

    private final SelectedUnitManager selectedUnitManager;

    private final ModeController modeController;

    private final UnitUtil unitUtil;

    private List<Location> moveLocations;

    private MoveMode moveMode;

    /**
     * Define what mode of move.
     *
     */
    public enum MoveMode {

        MOVE(ImageProvider.IMG_ICON_STEPS_25x25, ImageProvider.IMG_ICON_STEPS_TURN_25x25),

        ANCHOR(ImageProvider.IMG_ICON_STEPS_ANCHOR_25x25,
                ImageProvider.IMG_ICON_STEPS_ANCHOR_TURN_25x25),

        FIGHT(ImageProvider.IMG_ICON_STEPS_FIGHT_25x25,
                ImageProvider.IMG_ICON_STEPS_FIGHT_TURN_25x25);

        /**
         * Normal step image.
         */
        private final String image;

        /**
         * Step image indicating that here ends turn.
         */
        private final String turnImage;

        private MoveMode(final String image, final String turnImage) {
            this.image = Preconditions.checkNotNull(image);
            this.turnImage = Preconditions.checkNotNull(turnImage);
        }

        public String getImageForStep(final boolean normalStep) {
            if (normalStep) {
                return image;
            } else {
                return turnImage;
            }
        }

        public String getImage() {
            return image;
        }

        public String getTurnImage() {
            return turnImage;
        }

    }

    @Inject
    public MoveModeSupport(
            final SelectedTileManager selectedTileManager,
            final MouseOverTileManager mouseOverTileManager,
            final SelectedUnitManager selectedUnitManager, final ModeController modeController,
            final UnitUtil unitUtil) {
        this.selectedTileManager = Preconditions.checkNotNull(selectedTileManager);
        this.mouseOverTileManager = Preconditions.checkNotNull(mouseOverTileManager);
        this.selectedUnitManager = Preconditions.checkNotNull(selectedUnitManager);
        this.modeController = Preconditions.checkNotNull(modeController);
        this.unitUtil = Preconditions.checkNotNull(unitUtil);
        moveLocations = new ArrayList<>();
    }

    @Subscribe
    @SuppressWarnings("unused")
    private void onStartMove(final StartMoveEvent event) {
        recountPath();
    }

    @Subscribe
    private void onMouseOverTileChanged(final MouseOverTileChangedEvent mouseOverTileChangedEvent) {
        Preconditions.checkNotNull(mouseOverTileChangedEvent);
        logger.debug("Recounting path: " + mouseOverTileChangedEvent);
        if (modeController.isMoveMode()) {
            recountPath();
        } else {
            noMove();
        }
    }

    private void recountPath() {
        if (mouseOverTileManager.getMouseOverTile().isPresent()) {
            final Location target = mouseOverTileManager.getMouseOverTile().get();
            if (selectedTileManager.getSelectedTile().get().equals(target)) {
                /**
                 * Pointing with mouse to unit which should move.
                 */
                noMove();
            } else {
                processMove(target);
            }
        } else {
            noMove();
        }
    }

    private void processMove(final Location moveToLocation) {
        final Unit movingUnit = selectedUnitManager.getSelectedUnit().get();
        final UnitMove unitMove = new UnitMove(movingUnit, moveToLocation);
        if (movingUnit.isPossibleToAttackAt(moveToLocation)) {
            // fights
            moveLocations = Lists.newArrayList(
                    movingUnit.getPath(moveToLocation, true).orElse(Collections.emptyList()));
            moveLocations.add(moveToLocation);
            moveMode = MoveMode.FIGHT;
        } else if (movingUnit.isPossibleToEmbarkAt(moveToLocation)) {
            // embark
            moveLocations = movingUnit.getPath(moveToLocation)
                    .orElse(Lists.newArrayList(moveToLocation));
            moveMode = MoveMode.ANCHOR;
        } else if (unitUtil.isPossibleToDisembarkAt(movingUnit, moveToLocation)) {
            moveLocations = movingUnit.getPath(moveToLocation)
                    .orElse(Lists.newArrayList(moveToLocation));
            moveMode = MoveMode.ANCHOR;
        } else if (unitMove.isOneTurnMove()) {
            moveLocations = unitMove.getPath();
            moveMode = MoveMode.MOVE;
        } else {
            noMove();
        }
    }

    private void noMove() {
        moveLocations = new ArrayList<>();
        moveMode = MoveMode.MOVE;
    }

    public List<Location> getMoveLocations() {
        return moveLocations;
    }

    public MoveMode getMoveMode() {
        return moveMode;
    }

}
