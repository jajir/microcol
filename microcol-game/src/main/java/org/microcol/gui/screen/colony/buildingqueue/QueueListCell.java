package org.microcol.gui.screen.colony.buildingqueue;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardConst;
import org.microcol.gui.util.ClipboardParser;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.gui.util.From;
import org.microcol.model.ColonyBuildingItemProgress;
import org.microcol.model.ColonyBuildingItemProgressConstruction;
import org.microcol.model.ColonyBuildingItemProgressUnit;
import org.microcol.model.ConstructionType;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

public class QueueListCell extends ListCell<ColonyBuildingItemProgress<?>> {

    private final ImageProvider imageProvider;

    private final QueueController queueController;

    public QueueListCell(final QueueController queueController, final ImageProvider imageProvider) {
        this.queueController = Preconditions.checkNotNull(queueController);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
    }

    @Override
    protected void updateItem(final ColonyBuildingItemProgress<?> item, final boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            final HBox box = new HBox();
            box.getChildren().add(new Label("prase  "));
            box.getChildren().add(new Label(item.getName()));
            setGraphic(box);

            if (item instanceof ColonyBuildingItemProgressUnit) {
                final ColonyBuildingItemProgressUnit colonyItem = (ColonyBuildingItemProgressUnit) item;
                box.setOnDragDetected(event -> {
                    onDragDetected(event, box, colonyItem);
                });
            } else {
                final ColonyBuildingItemProgressConstruction constructionItem = (ColonyBuildingItemProgressConstruction) item;
                box.setOnDragDetected(event -> {
                    onDragDetected(event, box, constructionItem);
                });
            }
            final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(this,
                    this::isItValid);
            box.setOnDragEntered(backgroundHighlighter::onDragEntered);
            box.setOnDragExited(backgroundHighlighter::onDragExited);
            box.setOnDragOver(this::onDragOver);
            box.setOnDragDropped(event -> onDragDropped(event, item));
        }
    }

    private void onDragOver(final DragEvent event) {
        if (isItValid(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        }
    }

    private void onDragDropped(final DragEvent event, final ColonyBuildingItemProgress<?> item) {
        final ClipboardParser parser = ClipboardParser.make(event.getDragboard());
        final From from = parser.getFrom().get();
        if (from == From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION) {
            parser.get(ClipboardConst.KEY_CONSTRUCTION_TYPE);
            queueController.addBeforeItem(
                    ConstructionType.valueOf(parser.get(ClipboardConst.KEY_CONSTRUCTION_TYPE)),
                    item.getId());
            event.acceptTransferModes(TransferMode.MOVE);
            event.setDropCompleted(true);
            event.consume();
        } else if (from == From.VALUE_FROM_BUILDING_QUEUE_UNIT) {
            queueController.addBeforeItem(UnitType.valueOf(parser.get(ClipboardConst.KEY_UNIT_TYPE)),
                    item.getId());
            event.acceptTransferModes(TransferMode.MOVE);
            event.setDropCompleted(true);
            event.consume();
        } else if (from == From.VALUE_FROM_BUILDING_QUEUE) {
            int movingItemId = parser.getInt(ClipboardConst.KEY_INDEX);
            queueController.moveItem(movingItemId, item.getId());
            event.acceptTransferModes(TransferMode.MOVE);
            event.setDropCompleted(true);
            event.consume();
        }

    }

    private void onDragDetected(final MouseEvent mouseEvent, final Node node,
            final ColonyBuildingItemProgressConstruction item) {
        final Image image = imageProvider.getUnitImage(UnitType.COLONIST);
        final Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
        ClipboardWritter.make(db).addImage(image).fromBuildingQueue(item.getConstructionType())
                .addKeyValue(ClipboardConst.KEY_INDEX, item.getId()).build();
        mouseEvent.consume();
    }

    private void onDragDetected(final MouseEvent mouseEvent, final Node node,
            final ColonyBuildingItemProgressUnit item) {
        final Image image = imageProvider.getUnitImage(UnitType.COLONIST);
        final Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
        ClipboardWritter.make(db).addImage(image).fromBuildingQueue(item.getUnitType())
                .addKeyValue(ClipboardConst.KEY_INDEX, item.getId()).build();
        mouseEvent.consume();
    }

    private boolean isItValid(final Dragboard db) {
        final ClipboardParser parser = ClipboardParser.make(db);
        return parser.getFrom()
                .filter(from -> from == From.VALUE_FROM_BUILDING_QUEUE_CONSTRUCTION
                        || from == From.VALUE_FROM_BUILDING_QUEUE_UNIT
                        || from == From.VALUE_FROM_BUILDING_QUEUE)
                .isPresent();
    }

}
