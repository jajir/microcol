package org.microcol.gui.screen.colony.buildingqueue;

import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.colony.ColonyDialogCallback;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardConst;
import org.microcol.gui.util.ClipboardParser;
import org.microcol.gui.util.From;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.Colony;
import org.microcol.model.ColonyBuildingItemProgress;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

class PanelQueueConstructions extends TitledPanel {

    private final ColonyDialogCallback colonyDialogCallback;

    private final QueueController queueController;

    private final ImageProvider imageProvider;

    private final VBox queuePanel;

    @Inject
    public PanelQueueConstructions(final ColonyDialogCallback colonyDialogCallback,
            final ImageProvider imageProvider, final QueueController queueController) {
        super("Budovy");
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
        queueController.getObservableList().addListener(this::onListChangeEvent);
    }

    private void onListChangeEvent(
            @SuppressWarnings("unused") final ListChangeListener.Change<? extends ColonyBuildingItemProgress<?>> change) {
        repaint();
    }

    void repaint() {
        final Colony colony = colonyDialogCallback.getColony();
        queuePanel.getChildren().clear();
        queuePanel.getChildren().add(new Label("Fronto co stavim"));
        colony.getColonyBuildingQueue().getBuildigItemsConstruction()
                .forEach(colonyBuildingConstruction -> {
                    final ConstructionDraggablePanel panel = new ConstructionDraggablePanel(
                            colonyBuildingConstruction, imageProvider);
                    queuePanel.getChildren().add(panel.getPanel());
                });
    }

    private void onDragDropped(final DragEvent event) {
        final ClipboardParser parser = ClipboardParser.make(event.getDragboard());
        if (parser.getFrom().get() == From.VALUE_FROM_BUILDING_QUEUE) {
            int removingItemId = parser.getInt(ClipboardConst.KEY_INDEX);
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
