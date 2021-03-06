package org.microcol.gui.screen.colony;

import java.util.Optional;

import org.microcol.gui.dialog.DialogDestroyColony;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.model.Colony;
import org.microcol.model.Unit;
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

    private final Logger logger = LoggerFactory.getLogger(PanelOutsideColony.class);

    private final HBox panelUnits;

    private final GameModelController gameModelController;

    private final ColonyDialogCallback colonyDialog;

    private final DialogDestroyColony dialogDestroyColony;

    private final EventBus eventBus;

    private final PanelOutsideColonyUnitFactory panelOutsideColonyUnitFactory;

    private Colony colony;

    @Inject
    public PanelOutsideColony(final GameModelController gameModelController,
            final ColonyDialogCallback colonyDialog, final DialogDestroyColony dialogDestroyColony,
            final EventBus eventBus,
            final PanelOutsideColonyUnitFactory panelOutsideColonyUnitFactory) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.colonyDialog = Preconditions.checkNotNull(colonyDialog);
        this.dialogDestroyColony = Preconditions.checkNotNull(dialogDestroyColony);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.panelOutsideColonyUnitFactory = Preconditions
                .checkNotNull(panelOutsideColonyUnitFactory);
        panelUnits = new HBox();

        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(panelUnits,
                this::isItUnit);
        panelUnits.setOnDragEntered(backgroundHighlighter::onDragEntered);
        panelUnits.setOnDragExited(backgroundHighlighter::onDragExited);
        panelUnits.setOnDragDropped(this::onDragDropped);
        panelUnits.setOnDragOver(this::onDragOver);
        panelUnits.getStyleClass().add("outside");
    }

    void setColony(final Colony colony) {
        this.colony = colony;
    }

    void repaint() {
        panelUnits.getChildren().clear();
        colony.getUnitsOutSideColony().forEach(unit -> {
            final PanelOutsideColonyUnit paneImage = panelOutsideColonyUnitFactory.make(unit,
                    colony);
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
        return ClipboardEval.make(gameModelController.getModel(), db).getUnit().isPresent();
    }

    private void onDragDropped(final DragEvent event) {
        logger.debug("Object was dropped on panel outside colony.");
        final Optional<Unit> oUnit = ClipboardEval
                .make(gameModelController.getModel(), event.getDragboard()).getUnit();
        if (oUnit.isPresent()) {
            final Unit unit = oUnit.get();
            if (colony.isLastUnitIncolony(unit)) {
                if (dialogDestroyColony.showWaitAndReturnIfYesWasSelected()) {
                    unit.placeToMap(colony.getLocation());
                    eventBus.post(new UnitMovedOutsideColonyEvent(unit, colony));
                    colonyDialog.close();
                }
            } else {
                unit.placeToMap(colony.getLocation());
                eventBus.post(new UnitMovedOutsideColonyEvent(unit, colony));
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
