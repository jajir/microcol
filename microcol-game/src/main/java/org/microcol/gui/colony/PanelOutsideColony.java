package org.microcol.gui.colony;

import java.util.Optional;

import org.microcol.gui.DialogDestroyColony;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;
import org.microcol.model.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

/**
 * Show units outside colony.
 */
public final class PanelOutsideColony extends TitledPanel {

    private final Logger logger = LoggerFactory.getLogger(PanelOutsideColony.class);

    private final HBox panelUnits;

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private final ColonyDialogCallback colonyDialog;

    private final DialogDestroyColony dialogDestroyColony;

    private final UnitMovedOutsideColonyController unitMovedOutsideColonyController;

    private Colony colony;

    @Inject
    public PanelOutsideColony(final ImageProvider imageProvider,
            final GameModelController gameModelController, final ColonyDialogCallback colonyDialog,
            final DialogDestroyColony dialogDestroyColony,
            final UnitMovedOutsideColonyController unitMovedOutsideColonyController) {
        super("Outside Colony", null);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.colonyDialog = Preconditions.checkNotNull(colonyDialog);
        this.dialogDestroyColony = Preconditions.checkNotNull(dialogDestroyColony);
        this.unitMovedOutsideColonyController = Preconditions
                .checkNotNull(unitMovedOutsideColonyController);
        panelUnits = new HBox();
        getContentPane().getChildren().add(panelUnits);
        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(this,
                this::isItUnit);
        setOnDragEntered(backgroundHighlighter::onDragEntered);
        setOnDragExited(backgroundHighlighter::onDragExited);
        setOnDragDropped(this::onDragDropped);
        setOnDragOver(this::onDragOver);
    }

    public void setColony(final Colony colony) {
        this.colony = colony;
        panelUnits.getChildren().clear();
        colony.getUnitsOutSideColony().forEach(unit -> {
            final PanelUnitWithContextMenu paneImage = new PanelUnitWithContextMenu(imageProvider,
                    unit, colony, colonyDialog);
            panelUnits.getChildren().add(paneImage.getPane());
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
                    unitMovedOutsideColonyController
                            .fireEvent(new UnitMovedOutsideColonyEvent(unit, colony));
                    colonyDialog.close();
                }
            } else {
                unit.placeToMap(colony.getLocation());
                unitMovedOutsideColonyController
                        .fireEvent(new UnitMovedOutsideColonyEvent(unit, colony));
            }
            event.acceptTransferModes(TransferMode.MOVE);
            event.setDropCompleted(true);
            event.consume();
        }
    }

}
