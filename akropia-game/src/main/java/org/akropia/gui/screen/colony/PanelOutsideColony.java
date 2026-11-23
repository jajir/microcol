package org.akropia.gui.screen.colony;

import java.util.Optional;

import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.util.BackgroundHighlighter;
import org.akropia.gui.util.ClipboardEval;
import org.akropia.gui.util.JavaFxComponent;
import org.akropia.model.Colony;
import org.akropia.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Show units outside colony. Units should appear on some kind of pier.
 */
@Singleton
final class PanelOutsideColony implements JavaFxComponent {

    private final Logger logger = LoggerFactory
            .getLogger(PanelOutsideColony.class);

    private final HBox panelUnits;

    private final GameModelController gameModelController;

    private final EventBus eventBus;

    private final PanelOutsideColonyUnitFactory panelOutsideColonyUnitFactory;

    private Colony colony;

    @Inject
    public PanelOutsideColony(final GameModelController gameModelController,
            final EventBus eventBus,
            final PanelOutsideColonyUnitFactory panelOutsideColonyUnitFactory) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.panelOutsideColonyUnitFactory = Preconditions
                .checkNotNull(panelOutsideColonyUnitFactory);
        panelUnits = new HBox();

        panelUnits.getStyleClass().add("outside");
    }

    void setDropTarget(final Region target) {
        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(
                target, this::isItUnit);
        target.setOnDragEntered(backgroundHighlighter::onDragEntered);
        target.setOnDragExited(backgroundHighlighter::onDragExited);
        target.setOnDragDropped(this::onDragDropped);
        target.setOnDragOver(this::onDragOver);
    }

    void setColony(final Colony colony) {
        this.colony = colony;
    }

    void repaint() {
        panelUnits.getChildren().clear();
        colony.getUnitsOutSideColony().forEach(unit -> {
            final PanelOutsideColonyUnit paneImage = panelOutsideColonyUnitFactory
                    .make(unit, colony);
            panelUnits.getChildren().add(paneImage.getContent());
        });
    }

    private void onDragOver(final DragEvent event) {
        if (isItUnit(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    private boolean isItUnit(final Dragboard db) {
        return ClipboardEval.make(gameModelController.getModel(), db)
                .filterUnit(unit -> !unit.isAtCargoSlot()
                        || unit.canUnitDisembarkAt(colony.getLocation()))
                .getUnit().isPresent();
    }

    private void onDragDropped(final DragEvent event) {
        logger.debug("Object was dropped on panel outside colony.");
        final Optional<Unit> oUnit = ClipboardEval
                .make(gameModelController.getModel(), event.getDragboard())
                .getUnit();
        if (oUnit.isPresent()) {
            final Unit unit = oUnit.get();
            if (unit.isColonyTransferWaitingForMovement(colony,
                    colony.getLocation())) {
                event.setDropCompleted(false);
                event.consume();
                ColonyWorkDropRejectedEvent.afterDrag(event, eventBus,
                        ColonyMsg.fieldInsufficientMovement);
                return;
            }
            try {
                if (unit.isAtCargoSlot()) {
                    try {
                        unit.disembarkToLocation(colony.getLocation());
                    } catch (IllegalStateException rejected) {
                        event.setDropCompleted(false);
                        event.consume();
                        gameModelController.showPassengerTransferRejection();
                        return;
                    }
                    eventBus.post(
                            new UnitMovedOutsideColonyEvent(unit, colony));
                } else {
                    unit.placeToMap(colony.getLocation());
                    eventBus.post(
                            new UnitMovedOutsideColonyEvent(unit, colony));
                }
            } catch (IllegalStateException rejected) {
                event.setDropCompleted(false);
                event.consume();
                final ColonyMsg reason = unit
                        .isColonyTransferWaitingForMovement(colony,
                                colony.getLocation())
                                        ? ColonyMsg.fieldInsufficientMovement
                                        : ColonyMsg.jobChanged;
                ColonyWorkDropRejectedEvent.afterDrag(event, eventBus, reason);
                return;
            }
            event.acceptTransferModes(TransferMode.MOVE);
            event.setDropCompleted(true);
            event.consume();
        }
    }

    @Override
    public Region getContent() {
        return panelUnits;
    }

}
