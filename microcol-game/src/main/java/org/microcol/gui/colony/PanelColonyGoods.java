package org.microcol.gui.colony;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.From;
import org.microcol.gui.util.TitledPanel;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ColonyWarehouse;
import org.microcol.model.GoodType;
import org.microcol.model.GoodsAmount;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

/**
 * Show list of all available goods.
 */
public final class PanelColonyGoods extends TitledPanel {

    private final HBox hBox;

    private final GameModelController gameModelController;

    private final ColonyDialogCallback colonyDialog;

    private ColonyWarehouse colonyWarehouse;

    @Inject
    public PanelColonyGoods(final GameModelController gameModelController,
            final ImageProvider imageProvider, final ColonyDialogCallback colonyDialog) {
        super("zbozi");
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.colonyDialog = Preconditions.checkNotNull(colonyDialog);
        hBox = new HBox();
        getContentPane().getChildren().add(hBox);
        GoodType.BUYABLE_GOOD_TYPES.forEach(goodType -> {
            hBox.getChildren()
                    .add(new PanelColonyGood(imageProvider.getGoodTypeImage(goodType), goodType));
        });

        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(this,
                this::isItGoodAmount);
        setOnDragEntered(backgroundHighlighter::onDragEntered);
        setOnDragExited(backgroundHighlighter::onDragExited);

        setOnDragOver(this::onDragOver);
        setOnDragDropped(this::onDragDropped);
    }

    public void setColony(final Colony colony) {
        colonyWarehouse = Preconditions.checkNotNull(colony).getColonyWarehouse();
        hBox.getChildren().forEach(node -> {
            final PanelColonyGood panelColonyGood = (PanelColonyGood) node;
            panelColonyGood.setColony(colony);
        });
    }

    public void repaint() {
        hBox.getChildren().forEach(node -> {
            final PanelColonyGood panelColonyGood = (PanelColonyGood) node;
            panelColonyGood.repaint();
        });
    };

    private void onDragOver(final DragEvent event) {
        if (isItGoodAmount(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.ANY);
            event.consume();
        }
    }

    private void onDragDropped(final DragEvent event) {
        final ClipboardEval eval = ClipboardEval
                .make(gameModelController.getModel(), event.getDragboard())
                .filterFrom(from -> From.VALUE_FROM_UNIT == from);
        if (eval.getGoodAmount().isPresent() && eval.getCargoSlot().isPresent()) {
            final GoodsAmount goodAmount = eval.getGoodAmount().get();
            final CargoSlot fromCargoSlot = eval.getCargoSlot().get();
            colonyWarehouse.moveToWarehouse(goodAmount.getGoodType(), goodAmount.getAmount(),
                    fromCargoSlot);
            event.setDropCompleted(true);
            colonyDialog.repaint();
        }
        event.consume();
    }

    private boolean isItGoodAmount(final Dragboard db) {
        return ClipboardEval.make(gameModelController.getModel(), db).getGoodAmount().isPresent();
    }

}
