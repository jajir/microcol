package org.microcol.gui.europe;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.gui.util.ClipboardReader.TransferFromEuropePier;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.TitledPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

/**
 * Panels shows list of people already recruited. People from this panel could
 * be immediately embark.
 */
public final class PanelPortPier extends TitledPanel {

    private final Logger logger = LoggerFactory.getLogger(PanelPortPier.class);

    private final GameModelController gameModelController;

    private final EuropeDialogCallback europeDialog;

    private final ImageProvider imageProvider;

    private final LocalizationHelper localizationHelper;

    private final VBox panelUnits;

    @Inject
    public PanelPortPier(final GameModelController gameModelController,
            final EuropeDialogCallback europeDialogCallback, final Text text,
            final ImageProvider imageProvider, final LocalizationHelper localizationHelper) {
        super(text.get("europe.pier"), new Label(text.get("europe.pier")));
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.europeDialog = Preconditions.checkNotNull(europeDialogCallback);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.localizationHelper = Preconditions.checkNotNull(localizationHelper);
        panelUnits = new VBox();
        getContentPane().getChildren().add(panelUnits);
        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(this,
                this::isItCorrectObject);
        setOnDragEntered(backgroundHighlighter::onDragEntered);
        setOnDragExited(backgroundHighlighter::onDragExited);
        setOnDragOver(this::onDragOver);
        setOnDragDropped(this::onDragDropped);
    }

    void repaint() {
        panelUnits.getChildren().clear();
        gameModelController.getModel().getEurope().getPier()
                .getUnits(gameModelController.getCurrentPlayer())
                .forEach(unit -> panelUnits.getChildren()
                        .add(new PanelPortPierUnit(unit, imageProvider, localizationHelper)));
    }

    private void onDragOver(final DragEvent event) {
        if (isItCorrectObject(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    private void onDragDropped(final DragEvent event) {
        logger.debug("Object was dropped on ship cargo slot.");
        ClipboardReader.make(gameModelController.getModel(), event.getDragboard())
                .readUnit((draggedUnit, transferFrom) -> {
                    draggedUnit.placeToEuropePortPier();
                    europeDialog.repaintAfterGoodMoving();
                    event.acceptTransferModes(TransferMode.MOVE);
                    event.setDropCompleted(true);
                    event.consume();
                });
    }

    private boolean isItCorrectObject(final Dragboard db) {
        return ClipboardReader.make(gameModelController.getModel(), db)
                .filterUnit(unit -> !unit.getType().isShip())
                .filterTransferFrom(oTransferFrom -> oTransferFrom.isPresent()
                        && !(oTransferFrom.get() instanceof TransferFromEuropePier))
                .getUnit().isPresent();
    }

}
