package org.microcol.gui.colony;

import org.microcol.gui.DialogDestroyColony;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

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
            final Image image = imageProvider.getUnitImage(unit.getType());
            final ImageView imageView = new ImageView(image);
            Pane paneImage = new Pane(imageView);
            paneImage.setOnDragDetected(mouseEvent -> {
                final Dragboard db = imageView.startDragAndDrop(TransferMode.MOVE);
                ClipboardWritter.make(db).addImage(image).addTransferFromOutsideColony()
                        .addUnit(unit).build();
                mouseEvent.consume();
            });
            panelUnits.getChildren().add(paneImage);
        });
    }

    private void onDragOver(final DragEvent event) {
        if (isItUnit(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    private boolean isItUnit(final Dragboard db) {
        return ClipboardReader.make(gameModelController.getModel(), db).getUnit().isPresent();
    }

    private void onDragDropped(final DragEvent event) {
        logger.debug("Object was dropped on panel outside colony.");
        final Dragboard db = event.getDragboard();
        ClipboardReader.make(gameModelController.getModel(), db)
                .tryReadUnit((unit, transferFrom) -> {
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
                });
    }

}
