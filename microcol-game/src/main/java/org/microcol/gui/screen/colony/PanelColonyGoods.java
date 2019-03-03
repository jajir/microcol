package org.microcol.gui.screen.colony;

import java.util.List;

import org.microcol.gui.dialog.ChooseGoodAmountDialog;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.util.BackgroundHighlighter;
import org.microcol.gui.util.ClipboardEval;
import org.microcol.gui.util.From;
import org.microcol.gui.util.JavaFxComponent;
import org.microcol.i18n.I18n;
import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ColonyWarehouse;
import org.microcol.model.GoodType;
import org.microcol.model.GoodsAmount;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Show list of all available goods.
 */
public final class PanelColonyGoods implements JavaFxComponent {

    private final HBox hBox;

    private final GameModelController gameModelController;

    private final ColonyDialogCallback colonyDialog;

    private final List<PanelColonyGood> panelColonyGoods;

    private final TmpPanel mainPanel;

    private final ChooseGoodAmountDialog chooseGoodAmount;

    private ColonyWarehouse colonyWarehouse;

    @Inject
    public PanelColonyGoods(final GameModelController gameModelController,
            final ImageProvider imageProvider, final ColonyDialogCallback colonyDialog,
            final ChooseGoodAmountDialog chooseGoodAmount, final EventBus eventBus,
            final I18n i18n) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.colonyDialog = Preconditions.checkNotNull(colonyDialog);
        this.chooseGoodAmount = Preconditions.checkNotNull(chooseGoodAmount);
        hBox = new HBox();
        mainPanel = new TmpPanel();
        mainPanel.getContentPane().getChildren().add(hBox);
        panelColonyGoods = GoodType.BUYABLE_GOOD_TYPES.stream().map(goodType -> {
            final PanelColonyGood out = new PanelColonyGood(
                    imageProvider.getGoodTypeImage(goodType), goodType, eventBus, i18n);
            hBox.getChildren().add(out.getContent());
            return out;
        }).collect(ImmutableList.toImmutableList());
        mainPanel.getStyleClass().add("panel-colony-goods");

        final BackgroundHighlighter backgroundHighlighter = new BackgroundHighlighter(mainPanel,
                this::isItGoodAmount);
        mainPanel.setOnDragEntered(backgroundHighlighter::onDragEntered);
        mainPanel.setOnDragExited(backgroundHighlighter::onDragExited);

        mainPanel.setOnDragOver(this::onDragOver);
        mainPanel.setOnDragDropped(this::onDragDropped);
    }

    public void setColony(final Colony colony) {
        colonyWarehouse = Preconditions.checkNotNull(colony).getColonyWarehouse();
        panelColonyGoods.forEach(panelColonyGood -> panelColonyGood.setColony(colony));
    }

    public void repaint() {
        panelColonyGoods.forEach(panelColonyGood -> panelColonyGood.repaint());
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
            final GoodsAmount originalGoodsAmount = eval.getGoodAmount().get();
            final CargoSlot fromCargoSlot = eval.getCargoSlot().get();

            final GoodsAmount goodsAmount = adjustedAmount(originalGoodsAmount,
                    event.getTransferMode().equals(TransferMode.LINK));

            colonyWarehouse.moveToWarehouse(goodsAmount.getGoodType(), goodsAmount.getAmount(),
                    fromCargoSlot);
            event.setDropCompleted(true);
            colonyDialog.repaint();
        }
        event.consume();
    }

    private GoodsAmount adjustedAmount(final GoodsAmount originalAmount,
            final boolean specialOperationWasSelected) {
        chooseGoodAmount.init(originalAmount.getAmount());
        return new GoodsAmount(originalAmount.getGoodType(), chooseGoodAmount.getActualValue());
    }

    private boolean isItGoodAmount(final Dragboard db) {
        return ClipboardEval.make(gameModelController.getModel(), db).getGoodAmount().isPresent();
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

}
