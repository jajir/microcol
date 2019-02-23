package org.microcol.gui.screen.colony.buildingqueue;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.Clipboard;
import org.microcol.gui.util.ClipboardParser;
import org.microcol.gui.util.From;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.ColonyBuildingItemProgress;
import org.microcol.model.ConstructionType;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;

public class PanelQueueBuildingQueue extends TitledPanel {

    private final QueueController queueController;

    @Inject
    public PanelQueueBuildingQueue(final QueueController queueController,
            final ImageProvider imageProvider) {
        super("Co se bude stavet");
        this.queueController = Preconditions.checkNotNull(queueController);

        ListView<ColonyBuildingItemProgress<?>> list = new ListView<ColonyBuildingItemProgress<?>>();
        list.setItems(queueController.getObservableList());
        list.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        list.setEditable(false);
        list.setMouseTransparent(false);
        list.setFocusTraversable(false);
        list.setCellFactory(
                new Callback<ListView<ColonyBuildingItemProgress<?>>, ListCell<ColonyBuildingItemProgress<?>>>() {

                    @Override
                    public ListCell<ColonyBuildingItemProgress<?>> call(
                            ListView<ColonyBuildingItemProgress<?>> param) {
                        return new QueueListCell(queueController, imageProvider);
                    }

                });

        getContentPane().getChildren().add(list);

        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(this,
                this::isItValid);
        setOnDragEntered(backgroundHighlighter::onDragEntered);
        setOnDragExited(backgroundHighlighter::onDragExited);
        setOnDragOver(this::onDragOver);
        setOnDragDropped(this::onDragDropped);
    }

    private boolean isItValid(final Dragboard db) {
        final ClipboardParser parser = ClipboardParser.make(db);
        if (parser.getFrom().isPresent()) {
            final From from = parser.getFrom().get();
            return from == From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION
                    || from == From.VALUE_FROM_BUILDING_QUEUE_UNIT
                    || from == From.VALUE_FROM_BUILDING_QUEUE;
        } else {
            return false;
        }
    }

    void repaint() {
        queueController.repaint();
    }

    private void onDragOver(final DragEvent event) {
        if (isItValid(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    private void onDragDropped(final DragEvent event) {
        final Dragboard db = event.getDragboard();
        final ClipboardParser parser = ClipboardParser.make(db);
        final From from = parser.getFrom().get();
        if (from == From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION) {
            parser.get(Clipboard.KEY_CONSTRUCTION_TYPE);
            queueController.addAtEnd(
                    ConstructionType.valueOf(parser.get(Clipboard.KEY_CONSTRUCTION_TYPE)));
            event.acceptTransferModes(TransferMode.MOVE);
            event.setDropCompleted(true);
            event.consume();
        } else if (from == From.VALUE_FROM_BUILDING_QUEUE_UNIT) {
            queueController.addAtEnd(UnitType.valueOf(parser.get(Clipboard.KEY_UNIT_TYPE)));
            event.acceptTransferModes(TransferMode.MOVE);
            event.setDropCompleted(true);
            event.consume();
        } else if (from == From.VALUE_FROM_BUILDING_QUEUE) {
            int movingItemId = parser.getInt(Clipboard.KEY_INDEX);
            queueController.moveItemAtTheEnd(movingItemId);
            event.acceptTransferModes(TransferMode.MOVE);
            event.setDropCompleted(true);
            event.consume();
        }
    }

}
