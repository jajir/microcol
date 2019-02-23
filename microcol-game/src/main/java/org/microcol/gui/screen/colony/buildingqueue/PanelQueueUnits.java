package org.microcol.gui.screen.colony.buildingqueue;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.colony.ColonyDialogCallback;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.Clipboard;
import org.microcol.gui.util.ClipboardParser;
import org.microcol.gui.util.From;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

public class PanelQueueUnits extends TitledPanel {

    private final ColonyDialogCallback colonyDialogCallback;

    private final ImageProvider imageProvider;

    private final QueueController queueController;

    private final VBox queuePanel;

    @Inject
    public PanelQueueUnits(final ColonyDialogCallback colonyDialogCallback,
            final ImageProvider imageProvider, final QueueController queueController) {
        super("Jednotky");
        this.colonyDialogCallback = Preconditions.checkNotNull(colonyDialogCallback);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.queueController = Preconditions.checkNotNull(queueController);
        queuePanel = new VBox();
        getContentPane().getChildren().add(queuePanel);
        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(this,
                this::isItValid);
        setOnDragEntered(backgroundHighlighter::onDragEntered);
        setOnDragExited(backgroundHighlighter::onDragExited);
        setOnDragOver(this::onDragOver);
        setOnDragDropped(this::onDragDropped);
    }

    void repaint() {
        final Colony colony = colonyDialogCallback.getColony();
        queuePanel.getChildren().clear();
        queuePanel.getChildren().add(new Label("Jednotky, co muzu stavet"));
        colony.getColonyBuildingQueue().getBuildigItemsUnit().forEach(colonyBuildingItem -> {
            final UnitDraggablePanel panel = new UnitDraggablePanel(colonyBuildingItem,
                    imageProvider);
            queuePanel.getChildren().add(panel.getPanel());
        });
    }

    private void onDragDropped(final DragEvent event) {
        final ClipboardParser parser = ClipboardParser.make(event.getDragboard());
        if (parser.getFrom().get() == From.VALUE_FROM_BUILDING_QUEUE) {
            int removingItemId = parser.getInt(Clipboard.KEY_INDEX);
            queueController.removeItemById(removingItemId);
            event.acceptTransferModes(TransferMode.MOVE);
            event.setDropCompleted(true);
            event.consume();
        }

    }

    private void onDragOver(final DragEvent event) {
        if (isItValid(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    private boolean isItValid(final Dragboard db) {
        return ClipboardParser.make(db).getFrom()
                .filter(from -> from == From.VALUE_FROM_BUILDING_QUEUE).isPresent();
    }

}
